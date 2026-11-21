package pe.edu.upeu.solshiran.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.ErrorCatalogo
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleCabanaUseCase

class DetalleCabanaViewModel(
    private val idCabana: Long,
    private val obtenerDetalle: ObtenerDetalleCabanaUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(
        DetalleCabanaUiState()
    )

    val estado = _estado.asStateFlow()

    private var cargaActual: Job? = null

    init {
        cargarDetalle()
    }

    fun cargarDetalle() {
        if (cargaActual?.isActive == true) {
            return
        }

        _estado.update {
            it.copy(
                cargando = true,
                mensajeError = null
            )
        }

        cargaActual = viewModelScope.launch {
            try {
                val cabana = obtenerDetalle(idCabana)

                _estado.update {
                    it.copy(
                        cargando = false,
                        cabana = cabana,
                        mensajeError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorCatalogo) {
                mostrarError(
                    e.message ?: "No se pudo cargar el detalle."
                )
            } catch (e: IllegalArgumentException) {
                mostrarError(
                    e.message ?: "La cabaña no es válida."
                )
            } catch (_: Exception) {
                mostrarError(
                    "No se pudo cargar el detalle. " +
                            "Revisa tu conexión e inténtalo nuevamente."
                )
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        _estado.update {
            it.copy(
                cargando = false,
                cabana = null,
                mensajeError = mensaje
            )
        }
    }
}