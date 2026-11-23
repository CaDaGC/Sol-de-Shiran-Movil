package pe.edu.upeu.solshiran.presentation.pago

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.ErrorPago
import pe.edu.upeu.solshiran.domain.model.EstadoAdelanto
import pe.edu.upeu.solshiran.domain.model.PagoAdelanto
import pe.edu.upeu.solshiran.domain.usecase.ConsultarAdelantoUseCase
import pe.edu.upeu.solshiran.domain.usecase.CrearOrdenAdelantoUseCase

data class PagoAdelantoUiState(
    val idTransaccion: Long? = null,
    val urlCheckout: String? = null,
    val pago: PagoAdelanto? = null,
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val sesionInvalida: Boolean = false
) {
    val puedePreparar: Boolean
        get() = !cargando && !sesionInvalida && urlCheckout == null &&
            (idTransaccion == null || pago?.estado == EstadoAdelanto.PENDIENTE ||
                pago?.estado == EstadoAdelanto.RECHAZADA)
}

class PagoAdelantoViewModel(
    private val idReserva: Long,
    private val crearOrden: CrearOrdenAdelantoUseCase,
    private val consultarAdelanto: ConsultarAdelantoUseCase,
    private val datosGuardados: SavedStateHandle
) : ViewModel() {
    private val _estado = MutableStateFlow(
        PagoAdelantoUiState(idTransaccion = datosGuardados["idTransaccionAdelanto"])
    )
    val estado = _estado.asStateFlow()

    fun prepararPago() {
        if (!_estado.value.puedePreparar) return
        ejecutar {
            val orden = crearOrden(idReserva)
            datosGuardados["idTransaccionAdelanto"] = orden.idTransaccion
            _estado.update {
                it.copy(idTransaccion = orden.idTransaccion, urlCheckout = orden.url, pago = null)
            }
        }
    }

    fun consultarEstado() {
        val actual = _estado.value
        val id = actual.idTransaccion ?: return
        if (actual.pago?.estado == EstadoAdelanto.APROBADA) return
        ejecutar {
            val pago = consultarAdelanto(idReserva, id)
            _estado.update {
                it.copy(
                    pago = pago,
                    urlCheckout = it.urlCheckout.takeIf { pago.estado == EstadoAdelanto.PENDIENTE }
                )
            }
        }
    }

    fun avisarErrorNavegador() {
        _estado.update { it.copy(mensajeError = "No se pudo abrir Mercado Pago. Revisa que tengas un navegador disponible.") }
    }

    private fun ejecutar(operacion: suspend () -> Unit) {
        if (_estado.value.cargando || _estado.value.sesionInvalida) return
        _estado.update { it.copy(cargando = true, mensajeError = null) }
        viewModelScope.launch {
            try {
                operacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorPago) {
                _estado.update { it.copy(mensajeError = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update {
                    it.copy(mensajeError = "No se pudo verificar la operación de pago. Revisa tu conexión y reintenta.")
                }
            } finally {
                _estado.update { it.copy(cargando = false) }
            }
        }
    }
}
