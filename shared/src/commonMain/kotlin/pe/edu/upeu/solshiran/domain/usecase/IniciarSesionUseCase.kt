package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.domain.repository.AuthRepository

class IniciarSesionUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        correo: String,
        contrasena: String
    ): Sesion {
        val correoLimpio = correo.trim()

        require(correoLimpio.isNotBlank()) {
            "Ingresa tu correo electrónico."
        }

        require(
            Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                .matches(correoLimpio)
        ) {
            "Ingresa un correo electrónico válido."
        }

        require(contrasena.isNotBlank()) {
            "Ingresa tu contraseña."
        }

        return repository.iniciarSesion(
            correo = correoLimpio,
            contrasena = contrasena
        )
    }
}