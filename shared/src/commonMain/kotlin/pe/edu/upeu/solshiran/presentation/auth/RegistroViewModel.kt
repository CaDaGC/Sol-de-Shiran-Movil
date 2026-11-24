package pe.edu.upeu.solshiran.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.data.remote.RegistroApiService
import pe.edu.upeu.solshiran.data.remote.dto.RegistroClienteDto

data class RegistroUiState(
    val cargando: Boolean = false,
    val completado: Boolean = false,
    val error: String? = null
)

class RegistroViewModel(private val api: RegistroApiService) : ViewModel() {
    private val _estado = MutableStateFlow(RegistroUiState())
    val estado = _estado.asStateFlow()

    fun registrar(solicitud: RegistroClienteDto) {
        if (_estado.value.cargando || _estado.value.completado) return
        val persona = solicitud.persona
        val campos = listOf(
            solicitud.nombreUsuario.trim() to 80,
            persona.nombres.trim() to 100,
            persona.apellidos.trim() to 100,
            persona.tipoDocumento.trim() to 20,
            persona.numeroDocumento.trim() to 20,
            persona.telefono.trim() to 20,
            persona.direccion.trim() to 255
        )
        val error = when {
            campos.any { (texto, maximo) -> texto.isEmpty() || texto.length > maximo } ->
                "Completa todos los campos y revisa su longitud."
            solicitud.email.length > 150 ||
                !solicitud.email.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) ->
                "Ingresa un correo valido."
            solicitud.password.length !in 8..72 -> "La clave debe tener entre 8 y 72 caracteres."
            else -> null
        }
        if (error != null) {
            _estado.update { it.copy(error = error) }
            return
        }

        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                api.registrar(
                    solicitud.copy(
                        nombreUsuario = solicitud.nombreUsuario.trim(),
                        email = solicitud.email.trim(),
                        persona = persona.copy(
                            nombres = persona.nombres.trim(),
                            apellidos = persona.apellidos.trim(),
                            numeroDocumento = persona.numeroDocumento.trim(),
                            telefono = persona.telefono.trim(),
                            direccion = persona.direccion.trim()
                        )
                    )
                )
                _estado.update { it.copy(cargando = false, completado = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(cargando = false, error = e.message ?: "No se pudo crear la cuenta.") }
            }
        }
    }
}
