package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginaMisReservasDto(val content: List<MiReservaDto>, val page: Int, val totalPages: Int)

@Serializable
data class MiReservaDto(
    val idReserva: Long,
    val idUsuario: Long,
    val idCabana: Long,
    val nombreCabana: String? = null,
    val fechaIngreso: String,
    val fechaSalida: String,
    val cantidadHuespedes: Int,
    val tarifaNocheAplicada: Double,
    val montoTotal: Double,
    val montoAdelanto: Double,
    val saldoPendiente: Double,
    val estadoReserva: String,
    val horaIngreso: String? = null,
    val horaSalida: String? = null,
    val checkout: RegistroSalidaDto? = null
)
