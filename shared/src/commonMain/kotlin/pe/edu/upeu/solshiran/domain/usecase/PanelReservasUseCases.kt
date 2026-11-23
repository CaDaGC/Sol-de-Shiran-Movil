package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.repository.PanelReservasRepository

class ListarCabanasPanelUseCase(private val repository: PanelReservasRepository) {
    suspend operator fun invoke() = repository.listarCabanas()
}

class ConsultarPanelReservasUseCase(private val repository: PanelReservasRepository) {
    suspend operator fun invoke(filtro: FiltroPanelReservas) =
        repository.consultar(filtro.copy(buscar = filtro.buscar.trim()))
}

class ObtenerDetalleReservaPanelUseCase(private val repository: PanelReservasRepository) {
    suspend operator fun invoke(idReserva: Long): DetalleReservaPanel {
        if (idReserva <= 0) throw ErrorReserva("No se pudo identificar la reserva.")
        return repository.detalle(idReserva)
    }
}
