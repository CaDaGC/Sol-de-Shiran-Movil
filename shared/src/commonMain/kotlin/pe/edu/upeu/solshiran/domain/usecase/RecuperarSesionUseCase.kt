package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.domain.repository.AuthRepository

class RecuperarSesionUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Sesion? {
        return repository.recuperarSesion()
    }
}