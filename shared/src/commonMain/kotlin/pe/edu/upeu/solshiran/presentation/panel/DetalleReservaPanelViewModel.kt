package pe.edu.upeu.solshiran.presentation.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleReservaPanelUseCase

data class DetalleReservaPanelUiState(
    val detalle: DetalleReservaPanel? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val sesionInvalida: Boolean = false
)

class DetalleReservaPanelViewModel(
    private val idReserva: Long,
    private val obtenerDetalle: ObtenerDetalleReservaPanelUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(DetalleReservaPanelUiState())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun cargar() {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                val detalle = obtenerDetalle(idReserva)
                ensureActive()
                _estado.update { it.copy(detalle = detalle, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                _estado.update { it.copy(cargando = false, error = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(cargando = false, error = "No se pudo cargar el detalle de la reserva.") }
            }
        }
    }
}
