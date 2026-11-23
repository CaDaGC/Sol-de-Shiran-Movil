package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.OrdenAdelanto
import pe.edu.upeu.solshiran.domain.model.PagoAdelanto

interface PagoRepository {
    suspend fun crearOrden(idReserva: Long): OrdenAdelanto
    suspend fun consultarAdelanto(idReserva: Long, idTransaccion: Long): PagoAdelanto
}
