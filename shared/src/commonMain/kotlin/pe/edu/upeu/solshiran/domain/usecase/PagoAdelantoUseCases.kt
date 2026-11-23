package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.repository.PagoRepository

class CrearOrdenAdelantoUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(idReserva: Long) = repository.crearOrden(idReserva)
}

class ConsultarAdelantoUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(idReserva: Long, idTransaccion: Long) =
        repository.consultarAdelanto(idReserva, idTransaccion)
}
