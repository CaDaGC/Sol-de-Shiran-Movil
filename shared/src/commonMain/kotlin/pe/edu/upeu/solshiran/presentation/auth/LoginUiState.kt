package pe.edu.upeu.solshiran.presentation.auth

import pe.edu.upeu.solshiran.domain.model.Sesion

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val comprobandoSesion: Boolean = true,
    val mensaje: String? = null,
    val sesion: Sesion? = null
)