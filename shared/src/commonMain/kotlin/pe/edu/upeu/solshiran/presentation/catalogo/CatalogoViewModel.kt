package pe.edu.upeu.solshiran.presentation.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.ErrorCatalogo
import pe.edu.upeu.solshiran.domain.usecase.ListarCabanasUseCase

class CatalogoViewModel(
    private val listarCabanas: ListarCabanasUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogoUiState())
    val estado = _estado.asStateFlow()

    private var cargaActual: Job? = null

    fun cambiarBusqueda(valor: String) {
        _estado.update {
            it.copy(busqueda = valor)
        }
    }

    fun cargarCatalogo() {
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
                val cabanas = listarCabanas()

                _estado.update {
                    it.copy(
                        cabanas = cabanas,
                        cargando = false,
                        mensajeError = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorCatalogo) {
                mostrarError(
                    e.message ?: "No se pudo cargar el catálogo."
                )
            } catch (_: Exception) {
                mostrarError(
                    "No se pudo cargar el catálogo. " +
                            "Revisa tu conexión e inténtalo nuevamente."
                )
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        _estado.update {
            it.copy(
                cargando = false,
                mensajeError = mensaje
            )
        }
    }
}