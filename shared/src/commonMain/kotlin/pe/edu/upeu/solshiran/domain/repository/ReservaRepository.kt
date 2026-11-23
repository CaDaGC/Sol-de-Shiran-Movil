package pe.edu.upeu.solshiran.domain.repository

import kotlinx.datetime.LocalDate
import pe.edu.upeu.solshiran.domain.model.CotizacionReserva
import pe.edu.upeu.solshiran.domain.model.Estancia
import pe.edu.upeu.solshiran.domain.model.ReservaTemporal

interface ReservaRepository {
    suspend fun fechasOcupadas(idCabana: Long, desde: LocalDate, hasta: LocalDate): Set<LocalDate>
    suspend fun cotizar(estancia: Estancia): CotizacionReserva
    suspend fun registrar(estancia: Estancia): ReservaTemporal
}
