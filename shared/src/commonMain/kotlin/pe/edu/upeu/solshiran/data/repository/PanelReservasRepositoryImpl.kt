package pe.edu.upeu.solshiran.data.repository

import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.dto.SolicitudPagoRestanteDto
import pe.edu.upeu.solshiran.data.remote.PanelReservasApiService
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.model.SolicitudPagoRestante
import pe.edu.upeu.solshiran.domain.model.puedeConsultarPanelReservas
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.PanelReservasRepository

class PanelReservasRepositoryImpl(
    private val api: PanelReservasApiService,
    private val authRepository: AuthRepository
) : PanelReservasRepository {
    override suspend fun listarCabanas() = api.listarCabanas(token()).map { it.aDominio() }

    override suspend fun consultar(filtro: FiltroPanelReservas) =
        api.consultar(filtro, token()).aDominio()

    override suspend fun detalle(idReserva: Long) = api.detalle(idReserva, token()).aDominio()

    override suspend fun consultarPagoRestante(idReserva: Long) =
        api.consultarPagoRestante(idReserva, token())?.aDominio()

    override suspend fun registrarPagoRestante(idReserva: Long, solicitud: SolicitudPagoRestante) =
        api.registrarPagoRestante(
            idReserva,
            SolicitudPagoRestanteDto(
                metodoPago = solicitud.metodoPago.name,
                numeroOperacion = solicitud.numeroOperacion,
                claveIdempotencia = solicitud.claveIdempotencia
            ),
            token()
        ).aDominio()

    private suspend fun token(): String {
        val sesion = authRepository.recuperarSesion()
            ?: throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        if (!sesion.puedeConsultarPanelReservas()) {
            throw ErrorReserva("Tu cuenta no tiene permiso para consultar el panel de reservas.")
        }
        return sesion.token
    }
}
