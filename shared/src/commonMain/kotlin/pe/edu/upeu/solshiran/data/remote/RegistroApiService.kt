package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.RegistroClienteDto

class RegistroApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val url = "${configuracion.urlBase.trimEnd('/')}/usuarios/register"

    suspend fun registrar(solicitud: RegistroClienteDto) {
        val respuesta = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(solicitud)
        }
        when (respuesta.status.value) {
            201 -> Unit
            400 -> throw IllegalStateException("Revisa los datos ingresados.")
            409 -> throw IllegalStateException("El correo, usuario o documento ya existe, o faltan roles en el servidor.")
            else -> throw IllegalStateException("No se pudo crear la cuenta. Intenta nuevamente.")
        }
    }
}
