package pe.edu.upeu.solshiran.data.repository

import kotlinx.datetime.LocalDate
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.mapper.aDto
import pe.edu.upeu.solshiran.data.remote.ReservaApiService
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.Estancia
import pe.edu.upeu.solshiran.domain.model.ReservaTemporal
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.ReservaRepository

class ReservaRepositoryImpl(
    private val api: ReservaApiService,
    private val authRepository: AuthRepository
) : ReservaRepository {
    override suspend fun fechasOcupadas(idCabana: Long, desde: LocalDate, hasta: LocalDate) =
        api.fechasOcupadas(idCabana, desde.toString(), hasta.toString()).aDominio()

    override suspend fun cotizar(estancia: Estancia) = api.cotizar(estancia.aDto()).aDominio()

    override suspend fun registrar(estancia: Estancia): ReservaTemporal {
        val sesion = authRepository.recuperarSesion()
            ?: throw ErrorReserva("Tu sesión expiró. Inicia sesión nuevamente.", sesionInvalida = true)

        if (sesion.rol !in setOf("CLIENTE", "ADMIN")) {
            throw ErrorReserva("Tu cuenta no tiene permiso para reservar.")
        }

        return api.registrar(estancia.aDto(), sesion.token).aDominio()
    }
}
