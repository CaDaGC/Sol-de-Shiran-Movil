package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FechasOcupadasDto(val fechasOcupadas: List<String>)

@Serializable
data class CotizacionReservaDto(
    val disponible: Boolean,
    val noches: Long,
    val montoTotal: Double,
    val montoAdelanto: Double,
    val saldoPendiente: Double,
    val mensaje: String? = null
)

@Serializable
data class CrearReservaDto(
    val idCabana: Long,
    val fechaIngreso: String,
    val fechaSalida: String,
    val cantidadHuespedes: Int,
    val horaIngreso: String,
    val horaSalida: String
)

@Serializable
data class ReservaTemporalDto(
    val idReserva: Long,
    val codigo: String? = null,
    val expiraEn: String,
    val montoTotal: Double
)
