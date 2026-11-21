package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import pe.edu.upeu.solshiran.data.remote.dto.CabanaCatalogoDto
import pe.edu.upeu.solshiran.data.remote.dto.CabanaDetalleDto
import pe.edu.upeu.solshiran.domain.model.ErrorCatalogo

class CatalogoApiService(
    private val client: HttpClient,
    private val configuracion: ConfiguracionApi
) {
    suspend fun listar(
        token: String?
    ): List<CabanaCatalogoDto> {
        val respuesta = client.get(
            "${configuracion.urlBase.trimEnd('/')}/cabanas"
        ) {
            if (!token.isNullOrBlank()) {
                bearerAuth(token)
            }
        }

        return when (respuesta.status.value) {
            200 -> respuesta.body<List<CabanaCatalogoDto>>()

            401 -> throw ErrorCatalogo(
                "La sesión no es válida. Inicia sesión nuevamente."
            )

            403 -> throw ErrorCatalogo(
                "Tu cuenta no tiene acceso al catálogo."
            )

            in 500..599 -> throw ErrorCatalogo(
                "El catálogo no está disponible en este momento."
            )

            else -> throw ErrorCatalogo(
                "No se pudo obtener el catálogo."
            )
        }
    }

    suspend fun obtenerDetalle(
        idCabana: Long,
        token: String?
    ): CabanaDetalleDto {
        val respuesta = client.get(
            "${configuracion.urlBase.trimEnd('/')}/cabanas/$idCabana"
        ) {
            if (!token.isNullOrBlank()) {
                bearerAuth(token)
            }
        }

        return when (respuesta.status.value) {
            200 -> respuesta.body<CabanaDetalleDto>()

            401 -> throw ErrorCatalogo(
                "La sesión no es válida. Inicia sesión nuevamente."
            )

            403 -> throw ErrorCatalogo(
                "Tu cuenta no tiene acceso a esta información."
            )

            404 -> throw ErrorCatalogo(
                "La cabaña ya no está disponible en el catálogo."
            )

            in 500..599 -> throw ErrorCatalogo(
                "El detalle no está disponible en este momento."
            )

            else -> throw ErrorCatalogo(
                "No se pudo obtener el detalle de la cabaña."
            )
        }
    }
}