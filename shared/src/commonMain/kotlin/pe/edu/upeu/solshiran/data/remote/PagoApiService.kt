package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.CrearOrdenDto
import pe.edu.upeu.solshiran.data.remote.dto.OrdenAdelantoDto
import pe.edu.upeu.solshiran.data.remote.dto.PagoAdelantoDto
import pe.edu.upeu.solshiran.domain.model.ErrorPago

class PagoApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val url = "${configuracion.urlBase.trimEnd('/')}/pagos"

    suspend fun crearOrden(idReserva: Long, correo: String, token: String): OrdenAdelantoDto =
        client.post("$url/reservas/$idReserva/orden") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(CrearOrdenDto(correo))
        }.leer()

    suspend fun consultarEstado(idTransaccion: Long, token: String): PagoAdelantoDto =
        client.get("$url/transacciones/$idTransaccion") {
            bearerAuth(token)
        }.leer()

    private suspend inline fun <reified T> HttpResponse.leer(): T = when (status.value) {
        200, 201 -> body()
        400, 422 -> throw ErrorPago("No se pudo procesar la solicitud de pago. Verifica los datos de tu cuenta.")
        401 -> throw ErrorPago("La sesión no es válida. Inicia sesión nuevamente.", sesionInvalida = true)
        403 -> throw ErrorPago("Tu cuenta no tiene permiso para acceder a este pago.")
        404 -> throw ErrorPago("No se encontró la reserva o la transacción.")
        409 -> throw ErrorPago("El pago está en conflicto con el estado de la reserva o de un intento anterior.")
        502, 503, 504 -> throw ErrorPago("El servicio de pagos no pudo verificar la operación con Mercado Pago. Inténtalo nuevamente.")
        else -> throw ErrorPago("No se pudo completar la operación de pago. Inténtalo nuevamente.")
    }
}
