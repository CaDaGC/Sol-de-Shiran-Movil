package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SolicitudPagoRestanteDto(
    val metodoPago: String,
    val numeroOperacion: String? = null,
    val claveIdempotencia: String
)

@Serializable
data class PagoRestanteDto(
    val id: Long,
    val idReserva: Long,
    val claveIdempotencia: String,
    val monto: Double,
    val metodoPago: String,
    val numeroOperacion: String? = null,
    val completado: Boolean,
    val fechaRegistro: String? = null
)
