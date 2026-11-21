package pe.edu.upeu.solshiran.domain.model

data class Cabana(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val aforoMaximo: Int,
    val numeroHabitaciones: Int,
    val numeroBanos: Int,
    val tarifaNoche: Double,
    val imagenPrincipal: String?
)