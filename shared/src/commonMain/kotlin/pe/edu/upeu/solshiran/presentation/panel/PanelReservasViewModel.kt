package pe.edu.upeu.solshiran.presentation.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.CabanaPanel
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.model.PaginaReservas
import pe.edu.upeu.solshiran.domain.usecase.ConsultarPanelReservasUseCase
import pe.edu.upeu.solshiran.domain.usecase.ListarCabanasPanelUseCase

data class PanelReservasUiState(
    val filtro: FiltroPanelReservas = FiltroPanelReservas(),
    val cabanas: List<CabanaPanel> = emptyList(),
    val resultado: PaginaReservas? = null,
    val cargando: Boolean = true,
    val cargandoCabanas: Boolean = true,
    val error: String? = null,
    val errorCabanas: String? = null,
    val sesionInvalida: Boolean = false
) {
    val hayFiltros: Boolean get() = filtro.buscar.isNotBlank() || filtro.idCabana != null
}

class PanelReservasViewModel(
    private val consultar: ConsultarPanelReservasUseCase,
    private val listarCabanas: ListarCabanasPanelUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(PanelReservasUiState())
    val estado = _estado.asStateFlow()

    private var consultaActiva: Job? = null
    private var cargaCabanas: Job? = null

    init {
        actualizarCabanas()
        reintentar()
    }

    fun buscar(texto: String) {
        if (texto.length > 100) return
        cargar(_estado.value.filtro.copy(buscar = texto, pagina = 0), espera = 300)
    }

    fun seleccionarCabana(id: Long?) {
        val actual = _estado.value.filtro
        if (id != actual.idCabana) cargar(actual.copy(idCabana = id, pagina = 0))
    }

    fun limpiarFiltros() = cargar(FiltroPanelReservas())

    fun reintentar() = cargar(_estado.value.filtro)

    fun cambiarPagina(paso: Int) {
        val actual = _estado.value
        val resultado = actual.resultado ?: return
        val pagina = resultado.pagina + paso
        val puedeRetroceder = paso < 0 && pagina >= 0
        if (!actual.cargando && (puedeRetroceder || pagina in 0 until resultado.totalPaginas)) {
            cargar(actual.filtro.copy(pagina = pagina))
        }
    }

    fun actualizarCabanas() {
        if (cargaCabanas?.isActive == true || _estado.value.sesionInvalida) return
        _estado.update { it.copy(cargandoCabanas = true, errorCabanas = null) }
        cargaCabanas = viewModelScope.launch {
            try {
                val cabanas = listarCabanas()
                _estado.update { it.copy(cabanas = cabanas, cargandoCabanas = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                if (e.sesionInvalida) invalidarSesion()
                _estado.update { it.copy(cargandoCabanas = false, errorCabanas = e.message) }
            } catch (_: Exception) {
                _estado.update {
                    it.copy(cargandoCabanas = false, errorCabanas = "No se pudieron cargar las cabañas.")
                }
            }
        }
    }

    private fun cargar(filtro: FiltroPanelReservas, espera: Long = 0) {
        if (_estado.value.sesionInvalida) return
        consultaActiva?.cancel()
        _estado.update { it.copy(filtro = filtro, cargando = true, resultado = null, error = null) }
        consultaActiva = viewModelScope.launch {
            try {
                delay(espera)
                val resultado = consultar(filtro)
                ensureActive()
                _estado.update { it.copy(resultado = resultado, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                ensureActive()
                if (e.sesionInvalida) invalidarSesion()
                _estado.update { it.copy(cargando = false, error = e.message) }
            } catch (_: Exception) {
                ensureActive()
                _estado.update {
                    it.copy(cargando = false, error = "No se pudieron cargar las reservas. Revisa tu conexión.")
                }
            }
        }
    }

    private fun invalidarSesion() {
        _estado.update { it.copy(sesionInvalida = true, resultado = null, cabanas = emptyList()) }
        consultaActiva?.cancel()
        cargaCabanas?.cancel()
    }
}
