package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.CabanaPanelDto
import pe.edu.upeu.solshiran.data.remote.dto.PagoRestanteDto
import pe.edu.upeu.solshiran.data.remote.dto.PaginaReservasDto
import pe.edu.upeu.solshiran.data.remote.dto.ReservaDetallePanelDto
import pe.edu.upeu.solshiran.data.remote.dto.SolicitudPagoRestanteDto
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas

class PanelReservasApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val reservasUrl = "${configuracion.urlBase.trimEnd('/')}/reservas"
    private val pagosUrl = "${configuracion.urlBase.trimEnd('/')}/pagos"
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

    suspend fun consultarPagoRestante(idReserva: Long, token: String): PagoRestanteDto? =
        client.get("$pagosUrl/reservas/$idReserva/restante") {
            bearerAuth(token)
        }.leerPagoExistente()

    suspend fun registrarPagoRestante(
        idReserva: Long,
        solicitud: SolicitudPagoRestanteDto,
        token: String
    ): PagoRestanteDto = client.post("$pagosUrl/reservas/$idReserva/restante") {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(solicitud)
    }.leerPago()

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

    private suspend inline fun <reified T> HttpResponse.leerPago(): T = when (status.value) {
        200, 201 -> body()
        400, 422 -> throw ErrorReserva("Revisa el método de pago y el número de operación.")
        401 -> throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        403 -> throw ErrorReserva("Tu cuenta no tiene permiso para registrar el pago.")
        404 -> throw ErrorReserva("No se encontró la reserva.")
        409 -> throw ErrorReserva("La reserva cambió de estado o el pago ya fue procesado. Actualiza el detalle.")
        else -> throw ErrorReserva("No se pudo registrar el pago. Inténtalo nuevamente.")
    }

    private suspend fun HttpResponse.leerPagoExistente(): PagoRestanteDto? = when (status.value) {
        200 -> body()
        204 -> null
        401 -> throw ErrorReserva("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        403 -> throw ErrorReserva("Tu cuenta no tiene permiso para consultar el pago.")
        404 -> null
        else -> throw ErrorReserva("No se pudo consultar el pago existente.")
    }
}
