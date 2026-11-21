package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.repository.AuthRepository

class CerrarSesionUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.cerrarSesion()
    }
}