package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.solshiran.data.remote.dto.LoginRequestDto
import pe.edu.upeu.solshiran.data.remote.dto.LoginResponseDto
import pe.edu.upeu.solshiran.domain.model.ErrorAutenticacion

class AuthApiService(
    private val client: HttpClient,
    private val configuracion: ConfiguracionApi
) {
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): LoginResponseDto {
        val respuesta = client.post(
            "${configuracion.urlBase.trimEnd('/')}/auth/login"
        ) {
            contentType(ContentType.Application.Json)

            setBody(
                LoginRequestDto(
                    email = correo,
                    password = contrasena
                )
            )
        }

        return when (respuesta.status.value) {
            200 -> respuesta.body<LoginResponseDto>()

            400 -> throw ErrorAutenticacion(
                "Revisa el correo y la contraseña ingresados."
            )

            401 -> throw ErrorAutenticacion(
                "Correo o contraseña incorrectos."
            )

            403 -> throw ErrorAutenticacion(
                "No se pudo autorizar el inicio de sesión."
            )

            in 500..599 -> throw ErrorAutenticacion(
                "El servicio no está disponible. Inténtalo nuevamente."
            )

            else -> throw ErrorAutenticacion(
                "No se pudo iniciar sesión. Inténtalo nuevamente."
            )
        }
    }
}