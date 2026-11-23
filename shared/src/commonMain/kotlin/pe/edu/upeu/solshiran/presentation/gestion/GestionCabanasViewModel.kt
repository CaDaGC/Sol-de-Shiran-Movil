package pe.edu.upeu.solshiran.presentation.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.ErrorGestionCabanas
import pe.edu.upeu.solshiran.domain.model.FormularioCabana
import pe.edu.upeu.solshiran.domain.model.GestionCabana
import pe.edu.upeu.solshiran.domain.repository.GestionCabanasRepository

data class GestionCabanasUiState(
    val cabanas: List<GestionCabana> = emptyList(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensaje: String? = null,
    val sesionInvalida: Boolean = false
)

class GestionCabanasViewModel(private val repository: GestionCabanasRepository) : ViewModel() {
    private val _estado = MutableStateFlow(GestionCabanasUiState())
    val estado = _estado.asStateFlow()

    init { cargar() }

    fun limpiarGuardado() { _estado.update { it.copy(guardado = false) } }

    fun cargar() {
        _estado.update { it.copy(cargando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                val cabanas = repository.listar()
                _estado.update { it.copy(cabanas = cabanas, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorGestionCabanas) {
                _estado.update { it.copy(cargando = false, mensaje = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(cargando = false, mensaje = "No se pudo cargar la gestión de cabañas.") }
            }
        }
    }

    fun guardar(id: Long?, nombre: String, descripcion: String, aforo: String, habitaciones: String, banos: String, tarifa: String, imagen: String) {
        val request = runCatching {
            FormularioCabana(
                nombre = nombre.trim().also { require(it.isNotEmpty() && it.length <= 100) { "El nombre es obligatorio (máximo 100 caracteres)." } },
                descripcion = descripcion.trim().also { require(it.isNotEmpty() && it.length <= 10000) { "La descripción es obligatoria." } },
                aforoMaximo = aforo.toInt().also { require(it in 1..1000) { "El aforo debe estar entre 1 y 1000." } },
                habitaciones = habitaciones.toInt().also { require(it in 1..1000) { "Las habitaciones deben estar entre 1 y 1000." } },
                banos = banos.toInt().also { require(it in 1..1000) { "Los baños deben estar entre 1 y 1000." } },
                tarifaNoche = tarifa.toDouble().also { require(it.isFinite() && it in 0.01..99999999.99 && it.toString().substringAfter('.', "").length <= 2) { "La tarifa debe ser mayor a cero y tener hasta dos decimales." } },
                imagenPrincipal = imagen.trim().also { require(it.isNotEmpty() && it.length <= 255 && !it.any(Char::isWhitespace) && (it.startsWith("https://") || it.startsWith("http://") || it.startsWith("/"))) { "Usa una URL http/https o una ruta de imagen publicada." } }
            )
        }.getOrElse {
            _estado.update { state -> state.copy(mensaje = it.message ?: "Revisa los datos de la cabaña.") }
            return
        }
        _estado.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                repository.guardar(id, request)
                _estado.update { it.copy(guardando = false, guardado = true) }
                cargar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorGestionCabanas) {
                _estado.update { it.copy(guardando = false, mensaje = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(guardando = false, mensaje = "No se pudo guardar la cabaña.") }
            }
        }
    }

    fun cambiarEstado(cabana: GestionCabana, activar: Boolean) {
        _estado.update { it.copy(guardando = true, mensaje = null) }
        viewModelScope.launch {
            try {
                if (activar) repository.activar(cabana.id) else repository.desactivar(cabana.id)
                _estado.update { it.copy(guardando = false) }
                cargar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ErrorGestionCabanas) {
                _estado.update { it.copy(guardando = false, mensaje = e.message, sesionInvalida = e.sesionInvalida) }
            } catch (_: Exception) {
                _estado.update { it.copy(guardando = false, mensaje = "No se pudo actualizar el estado de la cabaña.") }
            }
        }
    }
}
