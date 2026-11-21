package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.CabanaDetalle
import pe.edu.upeu.solshiran.domain.repository.CabanaRepository

class ObtenerDetalleCabanaUseCase(
    private val repository: CabanaRepository
) {
    suspend operator fun invoke(
        idCabana: Long
    ): CabanaDetalle {
        require(idCabana > 0) {
            "La cabaña seleccionada no es válida."
        }

        return repository.obtenerDetalle(idCabana)
    }
}