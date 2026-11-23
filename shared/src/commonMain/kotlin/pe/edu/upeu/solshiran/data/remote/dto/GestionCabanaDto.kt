package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GestionCabanaDto(
    val idCabana: Long,
    val nombre: String,
    val descripcion: String,
    val aforoMaximo: Int,
    val numHabitaciones: Int,
    val numBanos: Int,
    val tarifaNoche: Double,
    val imagenPrincipal: String,
    val estado: String,
    val imagenesSecundarias: List<String> = emptyList()
)

@Serializable
data class GestionCabanaRequestDto(
    val nombre: String,
    val descripcion: String,
    val aforoMaximo: Int,
    val numHabitaciones: Int,
    val numBanos: Int,
    val tarifaNoche: Double,
    val imagenPrincipal: String,
    val imagenesSecundarias: List<String> = emptyList(),
    val duenoId: Long? = null
)
