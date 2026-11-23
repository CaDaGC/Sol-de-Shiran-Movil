package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.GestionCabanaDto
import pe.edu.upeu.solshiran.data.remote.dto.GestionCabanaRequestDto
import pe.edu.upeu.solshiran.domain.model.ErrorGestionCabanas

class GestionCabanasApiService(client: HttpClient, configuracion: ConfiguracionApi) {
    private val client = client
    private val url = "${configuracion.urlBase.trimEnd('/')}/cabanas"

    suspend fun listar(token: String): List<GestionCabanaDto> =
        client.get("$url/gestion") { bearerAuth(token) }.leer("No se pudieron cargar las cabañas.")

    suspend fun crear(token: String, request: GestionCabanaRequestDto): GestionCabanaDto =
        client.post(url) {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.leer("No se pudo crear la cabaña.")

    suspend fun editar(id: Long, token: String, request: GestionCabanaRequestDto): GestionCabanaDto =
        client.put("$url/$id") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.leer("No se pudo guardar los cambios de la cabaña.")

    suspend fun desactivar(id: Long, token: String) {
        client.delete("$url/$id") { bearerAuth(token) }.leerUnidad("No se pudo desactivar la cabaña.")
    }

    suspend fun activar(id: Long, token: String): GestionCabanaDto =
        client.put("$url/$id/activar") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(emptyMap<String, String>())
        }.leer("No se pudo activar la cabaña.")

    private suspend inline fun <reified T> HttpResponse.leer(mensaje: String): T = when (status.value) {
        200, 201 -> body()
        400, 422 -> throw ErrorGestionCabanas("Revisa los datos: nombre, descripción, capacidad, tarifa e imagen son obligatorios.")
        401 -> throw ErrorGestionCabanas("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
        403 -> throw ErrorGestionCabanas("Solo una cuenta administradora puede gestionar estas cabañas.")
        404 -> throw ErrorGestionCabanas("No se encontró la cabaña solicitada.")
        else -> throw ErrorGestionCabanas(mensaje)
    }

    private suspend fun HttpResponse.leerUnidad(mensaje: String) {
        when (status.value) {
            200, 204 -> Unit
            401 -> throw ErrorGestionCabanas("Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true)
            403 -> throw ErrorGestionCabanas("Solo una cuenta administradora puede gestionar estas cabañas.")
            404 -> throw ErrorGestionCabanas("No se encontró la cabaña solicitada.")
            else -> throw ErrorGestionCabanas(mensaje)
        }
    }
}
