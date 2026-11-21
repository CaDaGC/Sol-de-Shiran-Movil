package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CabanaDetalleDto(
    val idCabana: Long,
    val nombre: String,
    val descripcion: String? = null,
    val aforoMaximo: Int,
    val numHabitaciones: Int,
    val numBanos: Int,
    val tarifaNoche: Double,
    val imagenPrincipal: String? = null,
    val imagenesSecundarias: List<String> = emptyList()
)