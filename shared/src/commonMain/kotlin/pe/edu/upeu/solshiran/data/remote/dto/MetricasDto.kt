package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class IngresoDiaDto(val fecha: String, val total: Double, val operaciones: Long)

@Serializable
data class ResumenIngresosDto(
    val desde: String,
    val hasta: String,
    val totalIngresos: Double,
    val numeroPagos: Long,
    val ingresosPorDia: List<IngresoDiaDto>
)

@Serializable
data class FrecuenciaCabanaDto(val idCabana: Long, val reservas: Long)

@Serializable
data class ResumenReservasDto(
    val desde: String,
    val hasta: String,
    val totalReservas: Long,
    val ranking: List<FrecuenciaCabanaDto>
)
