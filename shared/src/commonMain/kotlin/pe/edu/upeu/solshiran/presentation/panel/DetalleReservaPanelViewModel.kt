package pe.edu.upeu.solshiran.presentation.panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid
import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.MetodoPagoRestante
import pe.edu.upeu.solshiran.domain.model.PagoRestanteRegistro
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleReservaPanelUseCase
import pe.edu.upeu.solshiran.domain.model.DanosReserva
import pe.edu.upeu.solshiran.domain.model.EstadoReservaPanel

data class DetalleReservaPanelUiState(
    val detalle: DetalleReservaPanel? = null,
    val cargando: Boolean = true,
    val error: String? = null,
    val pagoRestante: PagoRestanteRegistro? = null,
    val consultandoPago: Boolean = true,
    val registrandoPago: Boolean = false,
    val errorPago: String? = null,
    val registrandoSalida: Boolean = false,
    val errorSalida: String? = null,
    val sesionInvalida: Boolean = false
)

class DetalleReservaPanelViewModel(
    private val idReserva: Long,
    private val obtenerDetalle: ObtenerDetalleReservaPanelUseCase
) : ViewModel() {
    private val _estado = MutableStateFlow(DetalleReservaPanelUiState())
    val estado = _estado.asStateFlow()

    init { cargar() }

    private var claveIdempotencia = Uuid.random().toString()

    fun cargar() {
        _estado.update { it.copy(cargando = true, error = null, consultandoPago = true, errorPago = null) }
        viewModelScope.launch {
            try {
                val detalle = obtenerDetalle(idReserva)
                ensureActive()
                _estado.update { it.copy(detalle = detalle, cargando = false, errorPago = null) }
                try {
                    val pago = obtenerDetalle.consultarPago(idReserva)
                    ensureActive()
                    if (pago != null) claveIdempotencia = pago.claveIdempotencia
                    _estado.update { it.copy(pagoRestante = pago, consultandoPago = false) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: ErrorReserva) {
                    _estado.update { it.copy(errorPago = e.message, consultandoPago = false, sesionInvalida = e.sesionInvalida) }
                } catch (_: Exception) {
                    _estado.update { it.copy(errorPago = "No se pudo verificar si ya existe un pago registrado.", consultandoPago = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                _estado.update { it.copy(cargando = false, error = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(cargando = false, error = "No se pudo cargar el detalle de la reserva.") }
            }
        }
    }

    fun registrarSalida(danos: DanosReserva?) {
        if (_estado.value.registrandoSalida || _estado.value.detalle?.estado != EstadoReservaPanel.OCUPADA) return
        _estado.update { it.copy(registrandoSalida = true, errorSalida = null) }
        viewModelScope.launch {
            try {
                val salida = obtenerDetalle.registrarSalida(idReserva, danos)
                _estado.update { it.copy(registrandoSalida = false,
                    detalle = it.detalle?.copy(estado = EstadoReservaPanel.FINALIZADA, checkout = salida)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                _estado.update { it.copy(registrandoSalida = false, errorSalida = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(registrandoSalida = false, errorSalida = "No se pudo confirmar la salida. Reintenta con los mismos datos.") }
            }
        }
    }

    fun registrarPago(metodo: MetodoPagoRestante, numeroOperacion: String?) {
        if (_estado.value.registrandoPago || _estado.value.pagoRestante?.completado == true) return
        _estado.update { it.copy(registrandoPago = true, errorPago = null) }
        viewModelScope.launch {
            try {
                val pago = obtenerDetalle.registrarPago(idReserva, metodo, numeroOperacion, claveIdempotencia)
                ensureActive()
                if (!pago.completado) {
                    _estado.update { it.copy(registrandoPago = false, errorPago = "El servidor aún no confirma el pago. Puedes reintentar sin duplicarlo.") }
                    return@launch
                }
                claveIdempotencia = pago.claveIdempotencia
                _estado.update { it.copy(pagoRestante = pago, registrandoPago = false, errorPago = null) }
                cargar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorReserva) {
                _estado.update { it.copy(registrandoPago = false, errorPago = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(registrandoPago = false, errorPago = "No se pudo confirmar el pago. Reintenta para consultar el mismo registro.") }
            }
        }
    }
}
