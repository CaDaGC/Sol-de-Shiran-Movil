package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.model.MetodoPagoRestante
import pe.edu.upeu.solshiran.domain.model.PagoRestanteRegistro
import pe.edu.upeu.solshiran.domain.model.SolicitudPagoRestante
import pe.edu.upeu.solshiran.domain.repository.PanelReservasRepository
import pe.edu.upeu.solshiran.domain.model.DanosReserva

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

    suspend fun registrarSalida(idReserva: Long, danos: DanosReserva?) =
        repository.registrarSalida(idReserva, danos)

    suspend fun consultarPago(idReserva: Long) = repository.consultarPagoRestante(idReserva)

    suspend fun registrarPago(
        idReserva: Long,
        metodo: MetodoPagoRestante,
        numeroOperacion: String?,
        claveIdempotencia: String
    ): PagoRestanteRegistro {
        if (idReserva <= 0) throw ErrorReserva("No se pudo identificar la reserva.")
        val operacion = numeroOperacion?.trim()?.takeIf { it.isNotEmpty() }
        if (metodo != MetodoPagoRestante.EFECTIVO && operacion == null) {
            throw ErrorReserva("Ingresa el número de operación del pago.")
        }
        if (operacion != null && operacion.length > 100) {
            throw ErrorReserva("El número de operación no puede superar 100 caracteres.")
        }
        return repository.registrarPagoRestante(
            idReserva,
            SolicitudPagoRestante(metodo, operacion, claveIdempotencia)
        )
    }
}
