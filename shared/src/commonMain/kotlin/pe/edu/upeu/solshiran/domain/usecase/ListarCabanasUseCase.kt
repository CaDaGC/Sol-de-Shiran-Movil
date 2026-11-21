package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.Cabana
import pe.edu.upeu.solshiran.domain.repository.CabanaRepository

class ListarCabanasUseCase(
    private val repository: CabanaRepository
) {
    suspend operator fun invoke(): List<Cabana> {
        return repository.listar()
    }
}