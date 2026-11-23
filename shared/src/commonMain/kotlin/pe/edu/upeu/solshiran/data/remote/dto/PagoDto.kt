package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CrearOrdenDto(val payerEmail: String)

@Serializable
data class OrdenAdelantoDto(val idTransaccion: Long, val checkoutUrl: String)

@Serializable
data class PagoAdelantoDto(
    val idTransaccion: Long,
    val idReserva: Long,
    val estado: String,
    val monto: Double
)
