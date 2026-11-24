package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DanosReservaDto(val descripcion: String, val monto: Double, val medioPago: String)

@Serializable
data class SolicitudSalidaDto(val danos: DanosReservaDto? = null)

@Serializable
data class RegistroSalidaDto(
    val fecha: String,
    val descripcionDanos: String? = null,
    val montoDanos: Double? = null,
    val medioPago: String? = null
)

@Serializable
data class SalidaReservaDto(val idReserva: Long, val estadoReserva: String, val checkout: RegistroSalidaDto)
