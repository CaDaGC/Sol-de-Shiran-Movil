package pe.edu.upeu.solshiran.data.repository

import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.MisReservasApiService
import pe.edu.upeu.solshiran.domain.model.*
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.MisReservasRepository

class MisReservasRepositoryImpl(private val api: MisReservasApiService, private val auth: AuthRepository) : MisReservasRepository {
    override suspend fun consultar(grupo: GrupoMisReservas, pagina: Int): PaginaMisReservas {
        require(pagina >= 0) { "La página no es válida." }
        val sesion = cliente()
        val resultado = api.consultar(grupo, pagina, sesion.token)
        return PaginaMisReservas(resultado.content.map { it.aDominio(sesion.idUsuario) }, resultado.page, resultado.totalPages)
    }

    override suspend fun detalle(idReserva: Long): MiReserva {
        require(idReserva > 0) { "No se pudo identificar la reserva." }
        val sesion = cliente()
        val resultado = api.detalle(idReserva, sesion.token)
        if (resultado.idReserva != idReserva) throw ErrorReserva("El detalle recibido no corresponde a la reserva.")
        return resultado.aDominio(sesion.idUsuario)
    }

    private suspend fun cliente(): Sesion {
        val sesion = auth.recuperarSesion() ?: throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        if (sesion.rol != "CLIENTE") throw ErrorReserva("Inicia sesión como cliente para consultar tus reservas.")
        return sesion
    }
}
