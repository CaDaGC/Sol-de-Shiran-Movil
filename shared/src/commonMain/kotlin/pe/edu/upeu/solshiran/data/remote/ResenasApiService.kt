package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.ElegibilidadResenaDto
import pe.edu.upeu.solshiran.data.remote.dto.PaginaResenasDto
import pe.edu.upeu.solshiran.data.remote.dto.PublicarResenaDto

class ResenasApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val url = "${configuracion.urlBase.trimEnd('/')}/resenas/cabanas"

    suspend fun listar(idCabana: Long, pagina: Int): PaginaResenasDto {
        val respuesta = client.get("$url/$idCabana") { parameter("pagina", pagina) }
        if (respuesta.status.value != 200) throw IllegalStateException("No se pudieron cargar las resenas.")
        return respuesta.body()
    }

    suspend fun elegibilidad(idCabana: Long, token: String): ElegibilidadResenaDto {
        val respuesta = client.get("$url/$idCabana/elegibilidad") { bearerAuth(token) }
        return when (respuesta.status.value) {
            200 -> respuesta.body()
            401 -> throw IllegalStateException("Tu sesion vencio. Inicia sesion nuevamente.")
            else -> throw IllegalStateException("No se pudo comprobar si puedes publicar una resena.")
        }
    }

    suspend fun publicar(idCabana: Long, token: String, solicitud: PublicarResenaDto) {
        val respuesta = client.post("$url/$idCabana") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(solicitud)
        }
        when (respuesta.status.value) {
            201 -> Unit
            400 -> throw IllegalStateException("Revisa la puntuacion y el comentario.")
            401 -> throw IllegalStateException("Tu sesion vencio. Inicia sesion nuevamente.")
            403 -> throw IllegalStateException("Solo puedes opinar sobre una estancia tuya que ya finalizo.")
            409 -> throw IllegalStateException("Ya publicaste una resena para esta estancia.")
            else -> throw IllegalStateException("No se pudo publicar la resena. Intenta nuevamente.")
        }
    }
}
