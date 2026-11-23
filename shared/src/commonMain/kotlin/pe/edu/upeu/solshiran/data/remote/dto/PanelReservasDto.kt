package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CabanaPanelDto(val idCabana: Long, val nombre: String)

@Serializable
data class PaginaReservasDto(
    val content: List<ReservaPanelDto>,
    val page: Int,
    val totalElements: Long,
    val totalPages: Int
)

@Serializable
data class ReservaPanelDto(
    val idReserva: Long,
    val idUsuario: Long,
    val nombreUsuario: String? = null,
    val emailUsuario: String? = null,
    val idCabana: Long,
    val nombreCabana: String? = null,
    val fechaIngreso: String,
    val fechaSalida: String,
    val horaIngreso: String? = null,
    val horaSalida: String? = null,
    val cantidadHuespedes: Int,
    val montoTotal: Double,
    val saldoPendiente: Double,
    val estadoReserva: String,
    val codigo: String? = null
)
