package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import pe.edu.upeu.solshiran.data.remote.dto.CabanaPanelDto
import pe.edu.upeu.solshiran.data.remote.dto.PaginaReservasDto
import pe.edu.upeu.solshiran.data.remote.dto.ReservaDetallePanelDto
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas

class PanelReservasApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val reservasUrl = "${configuracion.urlBase.trimEnd('/')}/reservas"
    private val url = "$reservasUrl/panel"

    suspend fun listarCabanas(token: String): List<CabanaPanelDto> =
        client.get("$url/cabanas") { bearerAuth(token) }.leer()

    suspend fun consultar(filtro: FiltroPanelReservas, token: String): PaginaReservasDto =
        client.get(url) {
            bearerAuth(token)
            parameter("page", filtro.pagina)
            parameter("size", 20)
            if (filtro.buscar.isNotBlank()) parameter("buscar", filtro.buscar)
            filtro.idCabana?.let { parameter("idCabana", it) }
        }.leer()

    suspend fun detalle(idReserva: Long, token: String): ReservaDetallePanelDto =
        client.get("$reservasUrl/$idReserva") { bearerAuth(token) }.leerDetalle()

    private suspend inline fun <reified T> HttpResponse.leer(): T = when (status.value) {
        200 -> body()
        400, 422 -> throw ErrorReserva("Revisa los filtros de búsqueda.")
        401 -> throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        403 -> throw ErrorReserva("Tu cuenta no tiene permiso para consultar el panel de reservas.")
        else -> throw ErrorReserva("El panel de reservas no está disponible. Inténtalo nuevamente.")
    }

    private suspend inline fun <reified T> HttpResponse.leerDetalle(): T = when (status.value) {
        200 -> body()
        401 -> throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        403 -> throw ErrorReserva("Tu cuenta no tiene permiso para consultar esta reserva.")
        404 -> throw ErrorReserva("No se encontró la reserva solicitada.")
        else -> throw ErrorReserva("No se pudo cargar el detalle de la reserva. Inténtalo nuevamente.")
    }
}
