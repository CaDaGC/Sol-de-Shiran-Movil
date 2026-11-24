package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import pe.edu.upeu.solshiran.data.remote.dto.MiReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.PaginaMisReservasDto
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.GrupoMisReservas

class MisReservasApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val url = "${configuracion.urlBase.trimEnd('/')}/reservas"

    suspend fun consultar(grupo: GrupoMisReservas, pagina: Int, token: String): PaginaMisReservasDto =
        client.get("$url/mis-reservas") {
            bearerAuth(token)
            parameter("grupo", grupo.name)
            parameter("page", pagina)
            parameter("size", 20)
        }.leer()

    suspend fun detalle(idReserva: Long, token: String): MiReservaDto =
        client.get("$url/$idReserva") { bearerAuth(token) }.leer()

    private suspend inline fun <reified T> HttpResponse.leer(): T = when (status.value) {
        200 -> body()
        401 -> throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        403 -> throw ErrorReserva("No tienes permiso para consultar esta reserva.")
        404 -> throw ErrorReserva("No se encontró la reserva.")
        else -> throw ErrorReserva("No se pudieron cargar tus reservas. Inténtalo nuevamente.")
    }
}
