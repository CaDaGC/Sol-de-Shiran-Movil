package pe.edu.upeu.solshiran.presentation.metricas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import pe.edu.upeu.solshiran.data.remote.MetricasApiService
import pe.edu.upeu.solshiran.data.remote.dto.ResumenIngresosDto
import pe.edu.upeu.solshiran.data.remote.dto.ResumenReservasDto
import pe.edu.upeu.solshiran.domain.repository.AuthRepository

data class MetricasUiState(
    val desde: String,
    val hasta: String,
    val cargando: Boolean = true,
    val ingresos: ResumenIngresosDto? = null,
    val reservas: ResumenReservasDto? = null,
    val error: String? = null
)

class MetricasViewModel(
    private val api: MetricasApiService,
    private val auth: AuthRepository
) : ViewModel() {
    private val hoy = Clock.System.now().toLocalDateTime(TimeZone.of("America/Lima")).date
    private val _estado = MutableStateFlow(
        MetricasUiState(desde = hoy.plus(-29, DateTimeUnit.DAY).toString(), hasta = hoy.toString())
    )
    val estado = _estado.asStateFlow()
    private var carga: Job? = null

    init { cargar() }

    fun cambiarDesde(valor: String) = _estado.update { it.copy(desde = valor) }
    fun cambiarHasta(valor: String) = _estado.update { it.copy(hasta = valor) }

    fun cargar() {
        val desde = runCatching { LocalDate.parse(_estado.value.desde) }.getOrNull()
        val hasta = runCatching { LocalDate.parse(_estado.value.hasta) }.getOrNull()
        if (desde == null || hasta == null || hasta < desde || hasta > desde.plus(366, DateTimeUnit.DAY)) {
            _estado.update { it.copy(error = "Ingresa fechas validas en formato AAAA-MM-DD, hasta 367 dias.") }
            return
        }

        carga?.cancel()
        _estado.update { it.copy(cargando = true, error = null, ingresos = null, reservas = null) }
        carga = viewModelScope.launch {
            try {
                val sesion = auth.recuperarSesion()
                    ?: throw IllegalStateException("Inicia sesion para consultar las metricas.")
                if (sesion.rol !in setOf("ADMIN", "DUENO")) {
                    throw IllegalStateException("No tienes permiso para ver las metricas.")
                }
                val ingresos = api.ingresos(desde.toString(), hasta.toString(), sesion.token)
                val reservas = api.frecuencia(desde.toString(), hasta.toString(), sesion.token)
                _estado.update { it.copy(cargando = false, ingresos = ingresos, reservas = reservas) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.message ?: "No se pudieron cargar las metricas.") }
            }
        }
    }
}
