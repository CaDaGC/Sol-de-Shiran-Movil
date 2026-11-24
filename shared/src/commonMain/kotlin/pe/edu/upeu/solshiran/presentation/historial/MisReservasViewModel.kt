package pe.edu.upeu.solshiran.presentation.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.*
import pe.edu.upeu.solshiran.domain.usecase.ConsultarMisReservasUseCase

data class MisReservasUiState(
    val grupo: GrupoMisReservas = GrupoMisReservas.TODAS,
    val resultado: PaginaMisReservas? = null,
    val detalle: MiReserva? = null,
    val cargando: Boolean = true,
    val cargandoDetalle: Boolean = false,
    val error: String? = null,
    val errorDetalle: String? = null,
    val sesionInvalida: Boolean = false
)

class MisReservasViewModel(private val consultar: ConsultarMisReservasUseCase) : ViewModel() {
    private val _estado = MutableStateFlow(MisReservasUiState())
    val estado = _estado.asStateFlow()
    private var carga: Job? = null
    private var cargaDetalle: Job? = null

    init { cargar() }

    fun cargar(grupo: GrupoMisReservas = _estado.value.grupo, pagina: Int = 0) {
        carga?.cancel()
        cerrarDetalle()
        _estado.update { it.copy(grupo = grupo, cargando = true, resultado = null, error = null) }
        carga = viewModelScope.launch {
            try {
                val resultado = consultar(grupo, pagina)
                ensureActive()
                _estado.update { it.copy(resultado = resultado, cargando = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: ErrorReserva) {
                _estado.update { it.copy(cargando = false, error = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(cargando = false, error = "No se pudieron cargar tus reservas.") }
            }
        }
    }

    fun verDetalle(idReserva: Long) {
        cargaDetalle?.cancel()
        _estado.update { it.copy(detalle = null, cargandoDetalle = true, errorDetalle = null) }
        cargaDetalle = viewModelScope.launch {
            try {
                val respuesta = consultar.detalle(idReserva)
                val nombreCabana = _estado.value.resultado?.reservas?.firstOrNull { it.id == idReserva }?.cabana
                val detalle = respuesta.copy(cabana = nombreCabana ?: respuesta.cabana)
                ensureActive()
                _estado.update { it.copy(detalle = detalle, cargandoDetalle = false) }
            } catch (e: CancellationException) { throw e
            } catch (e: ErrorReserva) {
                _estado.update { it.copy(cargandoDetalle = false, errorDetalle = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(cargandoDetalle = false, errorDetalle = "No se pudo cargar el detalle de tu reserva.") }
            }
        }
    }

    fun cerrarDetalle() {
        cargaDetalle?.cancel()
        _estado.update { it.copy(detalle = null, cargandoDetalle = false, errorDetalle = null) }
    }
}
