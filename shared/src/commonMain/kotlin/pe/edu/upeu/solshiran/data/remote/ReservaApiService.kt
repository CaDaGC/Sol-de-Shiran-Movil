package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.CotizacionReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.CrearReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.FechasOcupadasDto
import pe.edu.upeu.solshiran.data.remote.dto.ReservaTemporalDto
import pe.edu.upeu.solshiran.domain.model.ErrorReserva

class ReservaApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val url = "${configuracion.urlBase.trimEnd('/')}/reservas"

    suspend fun fechasOcupadas(idCabana: Long, desde: String, hasta: String): FechasOcupadasDto =
        client.get("$url/cabanas/$idCabana/fechas-ocupadas") {
            parameter("desde", desde)
            parameter("hasta", hasta)
        }.leer()

    suspend fun cotizar(datos: CrearReservaDto): CotizacionReservaDto =
        client.get("$url/cabanas/${datos.idCabana}/disponibilidad") {
            parameter("fechaIngreso", datos.fechaIngreso)
            parameter("fechaSalida", datos.fechaSalida)
            parameter("cantidadHuespedes", datos.cantidadHuespedes)
            parameter("horaIngreso", datos.horaIngreso)
            parameter("horaSalida", datos.horaSalida)
        }.leer()

    suspend fun registrar(datos: CrearReservaDto, token: String): ReservaTemporalDto =
        client.post(url) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(datos)
        }.leer(autenticada = true)

    private suspend inline fun <reified T> HttpResponse.leer(autenticada: Boolean = false): T =
        when (status.value) {
            200, 201 -> body()
            400, 422 -> throw ErrorReserva("Revisa las fechas y la cantidad de huéspedes.")
            401 -> throw ErrorReserva("La sesión no es válida. Inicia sesión nuevamente.", autenticada)
            403 -> throw ErrorReserva("Tu cuenta no tiene permiso para realizar esta operación.")
            404 -> throw ErrorReserva("La cabaña ya no está disponible.")
            409 -> throw ErrorReserva("Las fechas ya no están disponibles. Vuelve a consultar.")
            else -> throw ErrorReserva("El servicio de reservas no está disponible. Inténtalo nuevamente.")
        }
}
