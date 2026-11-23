package pe.edu.upeu.solshiran.data.repository

import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.PagoApiService
import pe.edu.upeu.solshiran.domain.model.ErrorPago
import pe.edu.upeu.solshiran.domain.model.PagoAdelanto
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.PagoRepository

class PagoRepositoryImpl(
    private val api: PagoApiService,
    private val authRepository: AuthRepository
) : PagoRepository {
    override suspend fun crearOrden(idReserva: Long) = conSesion().let { sesion ->
        api.crearOrden(idReserva, sesion.correo, sesion.token).aDominio()
    }

    override suspend fun consultarAdelanto(idReserva: Long, idTransaccion: Long): PagoAdelanto {
        val pago = api.consultarEstado(idTransaccion, conSesion().token)
        if (pago.idReserva != idReserva || pago.idTransaccion != idTransaccion) {
            throw ErrorPago("La transacción no corresponde a esta reserva.")
        }
        return pago.aDominio()
    }

    private suspend fun conSesion() = authRepository.recuperarSesion()
        ?.also {
            if (it.rol !in setOf("CLIENTE", "ADMIN")) throw ErrorPago("Tu cuenta no tiene permiso para pagar.")
        }
        ?: throw ErrorPago("Tu sesión expiró. Inicia sesión nuevamente.", sesionInvalida = true)
}
