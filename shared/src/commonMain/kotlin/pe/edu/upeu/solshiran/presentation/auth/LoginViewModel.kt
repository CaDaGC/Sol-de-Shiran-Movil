package pe.edu.upeu.solshiran.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.solshiran.domain.model.ErrorAutenticacion
import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.RecuperarSesionUseCase

class LoginViewModel(
    private val iniciarSesion: IniciarSesionUseCase,
    private val recuperarSesion: RecuperarSesionUseCase,
    private val cerrarSesion: CerrarSesionUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado = _estado.asStateFlow()

    private var vigilanciaSesion: Job? = null

    init {
        recuperarSesionGuardada()
    }

    fun cambiarCorreo(valor: String) {
        _estado.update {
            it.copy(correo = valor, mensaje = null)
        }
    }

    fun cambiarContrasena(valor: String) {
        _estado.update {
            it.copy(contrasena = valor, mensaje = null)
        }
    }

    fun ingresar() {
        if (_estado.value.cargando ||
            _estado.value.comprobandoSesion
        ) {
            return
        }

        val correo = _estado.value.correo
        val contrasena = _estado.value.contrasena

        _estado.update {
            it.copy(cargando = true, mensaje = null)
        }

        viewModelScope.launch {
            try {
                val sesion = iniciarSesion(
                    correo = correo,
                    contrasena = contrasena
                )

                aceptarSesion(sesion)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                mostrarError(
                    e.message ?: "Revisa los datos ingresados."
                )
            } catch (e: ErrorAutenticacion) {
                mostrarError(
                    e.message ?: "No se pudo iniciar sesión."
                )
            } catch (_: Exception) {
                mostrarError(
                    "No se pudo completar el inicio de sesión. " +
                            "Revisa tu conexión e inténtalo nuevamente."
                )
            }
        }
    }

    fun salir() {
        terminarSesion("Has cerrado sesión.")
    }

    fun validarVigencia() {
        val sesion = _estado.value.sesion ?: return

        if (!sesion.estaVigente(Clock.System.now().epochSeconds)) {
            terminarSesion(
                "Tu sesión expiró. Inicia sesión nuevamente."
            )
        }
    }

    private fun recuperarSesionGuardada() {
        viewModelScope.launch {
            try {
                val sesion = recuperarSesion()

                if (sesion != null) {
                    aceptarSesion(sesion)
                } else {
                    _estado.update {
                        it.copy(comprobandoSesion = false)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _estado.update {
                    it.copy(
                        comprobandoSesion = false,
                        mensaje = "No se pudo recuperar la sesión. " +
                                "Inicia sesión nuevamente."
                    )
                }
            }
        }
    }

    private fun aceptarSesion(sesion: Sesion) {
        _estado.update {
            it.copy(
                correo = sesion.correo,
                contrasena = "",
                cargando = false,
                comprobandoSesion = false,
                mensaje = null,
                sesion = sesion
            )
        }

        vigilanciaSesion?.cancel()

        vigilanciaSesion = viewModelScope.launch {
            while (true) {
                val segundosRestantes = sesion.expiraEn -
                        Clock.System.now().epochSeconds

                if (segundosRestantes <= 0) {
                    terminarSesion(
                        "Tu sesión expiró. Inicia sesión nuevamente."
                    )
                    break
                }

                delay(1_000)
            }
        }
    }

    private fun terminarSesion(mensaje: String) {
        vigilanciaSesion?.cancel()
        vigilanciaSesion = null

        _estado.update {
            it.copy(
                sesion = null,
                contrasena = "",
                cargando = true,
                mensaje = mensaje
            )
        }

        viewModelScope.launch {
            try {
                cerrarSesion()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _estado.update {
                    it.copy(
                        mensaje = "No se pudo borrar la sesión guardada. " +
                                "Inténtalo nuevamente."
                    )
                }
            } finally {
                _estado.update {
                    it.copy(cargando = false)
                }
            }
        }
    }

    private fun mostrarError(mensaje: String) {
        _estado.update {
            it.copy(
                cargando = false,
                mensaje = mensaje
            )
        }
    }
}