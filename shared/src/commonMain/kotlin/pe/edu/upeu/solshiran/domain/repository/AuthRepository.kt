package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Sesion

    suspend fun recuperarSesion(): Sesion?

    suspend fun cerrarSesion()
}