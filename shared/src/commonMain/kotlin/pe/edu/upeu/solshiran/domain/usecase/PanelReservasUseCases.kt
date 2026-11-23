package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.repository.PanelReservasRepository

class ListarCabanasPanelUseCase(private val repository: PanelReservasRepository) {
    suspend operator fun invoke() = repository.listarCabanas()
}

class ConsultarPanelReservasUseCase(private val repository: PanelReservasRepository) {
    suspend operator fun invoke(filtro: FiltroPanelReservas) =
        repository.consultar(filtro.copy(buscar = filtro.buscar.trim()))
}
