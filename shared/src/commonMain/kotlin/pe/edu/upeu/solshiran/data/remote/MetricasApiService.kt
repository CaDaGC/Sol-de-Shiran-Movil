package pe.edu.upeu.solshiran.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.edu.upeu.solshiran.data.remote.dto.ResumenIngresosDto
import pe.edu.upeu.solshiran.data.remote.dto.ResumenReservasDto

class MetricasApiService(private val client: HttpClient, configuracion: ConfiguracionApi) {
    private val base = configuracion.urlBase.trimEnd('/')

    suspend fun ingresos(desde: String, hasta: String, token: String): ResumenIngresosDto {
        val respuesta = client.get("$base/pagos/metricas/ingresos") {
            bearerAuth(token)
            parameter("desde", desde)
            parameter("hasta", hasta)
        }
        return when (respuesta.status.value) {
            200 -> respuesta.body()
            401 -> throw IllegalStateException("Tu sesion vencio. Inicia sesion nuevamente.")
            403 -> throw IllegalStateException("No tienes permiso para ver las metricas.")
            else -> throw IllegalStateException("No se pudieron cargar los ingresos.")
        }
    }

    suspend fun frecuencia(desde: String, hasta: String, token: String): ResumenReservasDto {
        val respuesta = client.get("$base/reservas/metricas/frecuencia") {
            bearerAuth(token)
            parameter("desde", desde)
            parameter("hasta", hasta)
        }
        return when (respuesta.status.value) {
            200 -> respuesta.body()
            401 -> throw IllegalStateException("Tu sesion vencio. Inicia sesion nuevamente.")
            403 -> throw IllegalStateException("No tienes permiso para ver las metricas.")
            else -> throw IllegalStateException("No se pudo cargar la frecuencia de reservas.")
        }
    }
}
