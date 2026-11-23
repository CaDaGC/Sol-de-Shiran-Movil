package pe.edu.upeu.solshiran.presentation.reserva

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import pe.edu.upeu.solshiran.domain.model.CabanaDetalle
import pe.edu.upeu.solshiran.domain.model.CotizacionReserva
import pe.edu.upeu.solshiran.domain.model.ErrorCatalogo
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.Estancia
import pe.edu.upeu.solshiran.domain.model.ReservaTemporal
import pe.edu.upeu.solshiran.domain.usecase.ConsultarDisponibilidadUseCase
import pe.edu.upeu.solshiran.domain.usecase.ConsultarFechasOcupadasUseCase
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleCabanaUseCase
import pe.edu.upeu.solshiran.domain.usecase.RegistrarReservaUseCase
import pe.edu.upeu.solshiran.domain.usecase.hoyEnLima

data class ReservaUiState(
    val mes: LocalDate,
    val cabana: CabanaDetalle? = null,
    val errorCabana: String? = null,
    val ocupadas: Set<LocalDate>? = null,
    val errorCalendario: String? = null,
    val ingreso: LocalDate? = null,
    val salida: LocalDate? = null,
    val huespedes: String = "1",
    val cotizacion: CotizacionReserva? = null,
    val reserva: ReservaTemporal? = null,
    val procesando: Boolean = false,
    val errorOperacion: String? = null,
    val sesionInvalida: Boolean = false
)

class ReservaViewModel(
    private val idCabana: Long,
    private val obtenerDetalle: ObtenerDetalleCabanaUseCase,
    private val consultarFechas: ConsultarFechasOcupadasUseCase,
    private val consultarDisponibilidad: ConsultarDisponibilidadUseCase,
    private val registrarReserva: RegistrarReservaUseCase,
    private val datosGuardados: SavedStateHandle
) : ViewModel() {
    val hoy = hoyEnLima()
    val mesInicial = LocalDate(hoy.year, hoy.month, 1)
    private val _estado = MutableStateFlow(
        ReservaUiState(
            mes = mesInicial,
            reserva = datosGuardados.get<String>("reservaTemporal")?.let { Json.decodeFromString<ReservaTemporal>(it) },
            ingreso = datosGuardados.get<String>("ingresoReserva")?.let { LocalDate.parse(it) },
            salida = datosGuardados.get<String>("salidaReserva")?.let { LocalDate.parse(it) }
        )
    )
    val estado = _estado.asStateFlow()

    private var cargaCabana: Job? = null
    private var cargaCalendario: Job? = null

    init {
        if (_estado.value.reserva == null) {
            cargarCabana()
            cargarCalendario()
        }
    }

    fun cargarCabana() {
        if (cargaCabana?.isActive == true) return
        _estado.update { it.copy(errorCabana = null) }
        cargaCabana = viewModelScope.launch {
            try {
                val cabana = obtenerDetalle(idCabana)
                _estado.update { it.copy(cabana = cabana) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorCatalogo) {
                _estado.update { it.copy(errorCabana = e.message) }
            } catch (_: Exception) {
                _estado.update { it.copy(errorCabana = "No se pudo cargar la cabaña. Reintenta.") }
            }
        }
    }

    fun cargarCalendario(mes: LocalDate = _estado.value.mes) {
        cargaCalendario?.cancel()
        _estado.update { it.copy(mes = mes, ocupadas = null, errorCalendario = null) }
        cargaCalendario = viewModelScope.launch {
            try {
                val ultimoDia = mes.plus(1, DateTimeUnit.MONTH).plus(-1, DateTimeUnit.DAY)
                val ocupadas = consultarFechas(idCabana, mes, ultimoDia)
                _estado.update { it.copy(ocupadas = ocupadas) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _estado.update {
                    it.copy(errorCalendario = "No se pudieron consultar las fechas ocupadas.")
                }
            }
        }
    }

    fun cambiarMes(paso: Int) {
        val mes = _estado.value.mes.plus(paso, DateTimeUnit.MONTH)
        if (mes >= mesInicial) cargarCalendario(mes)
    }

    fun seleccionarFecha(fecha: LocalDate) {
        if (fecha < hoy) return
        editarEstancia { actual ->
            val ingreso = actual.ingreso
            if (ingreso != null && actual.salida == null && fecha > ingreso) {
                actual.copy(salida = fecha)
            } else {
                actual.copy(ingreso = fecha, salida = null)
            }
        }
    }

    fun cambiarHuespedes(valor: String) {
        if (valor.length <= 9 && valor.all { it in '0'..'9' }) {
            editarEstancia { it.copy(huespedes = valor) }
        }
    }

    private fun editarEstancia(cambio: (ReservaUiState) -> ReservaUiState) {
        _estado.update { actual ->
            if (actual.procesando || actual.reserva != null) actual
            else cambio(actual).copy(cotizacion = null, errorOperacion = null)
        }
    }

    fun cotizar() = ejecutar { estancia, aforo ->
        val cotizacion = consultarDisponibilidad(estancia, aforo)
        _estado.update { it.copy(cotizacion = cotizacion) }
    }

    fun reservar() {
        if (_estado.value.cotizacion?.disponible != true) return
        ejecutar(registrando = true) { estancia, aforo ->
            val reserva = registrarReserva(estancia, aforo)
            datosGuardados["reservaTemporal"] = Json.encodeToString(reserva)
            datosGuardados["ingresoReserva"] = estancia.fechaIngreso.toString()
            datosGuardados["salidaReserva"] = estancia.fechaSalida.toString()
            _estado.update { it.copy(reserva = reserva) }
        }
    }

    private fun ejecutar(
        registrando: Boolean = false,
        operacion: suspend (Estancia, Int) -> Unit
    ) {
        val actual = _estado.value
        if (actual.procesando || actual.reserva != null || actual.sesionInvalida) return
        val cabana = actual.cabana ?: return
        val ingreso = actual.ingreso ?: return
        val salida = actual.salida ?: return
        val estancia = Estancia(idCabana, ingreso, salida, actual.huespedes.toIntOrNull() ?: 0)

        _estado.update {
            it.copy(procesando = true, errorOperacion = null, cotizacion = null)
        }
        viewModelScope.launch {
            try {
                operacion(estancia, cabana.aforoMaximo)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                _estado.update {
                    it.copy(errorOperacion = e.message, sesionInvalida = e.sesionInvalida)
                }
                if (registrando && !e.sesionInvalida) cargarCalendario()
            } catch (_: Exception) {
                val mensaje = if (registrando) {
                    "No se pudo verificar el resultado de la reserva. Consulta las fechas antes de intentarlo otra vez."
                } else {
                    "No se pudo consultar la disponibilidad. Revisa tu conexión y reintenta."
                }
                _estado.update { it.copy(errorOperacion = mensaje) }
            } finally {
                _estado.update { it.copy(procesando = false) }
            }
        }
    }
}
