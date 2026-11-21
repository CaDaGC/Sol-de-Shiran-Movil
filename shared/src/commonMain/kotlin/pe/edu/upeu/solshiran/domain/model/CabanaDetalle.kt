package pe.edu.upeu.solshiran.domain.model

data class CabanaDetalle(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val aforoMaximo: Int,
    val numeroHabitaciones: Int,
    val numeroBanos: Int,
    val tarifaNoche: Double,
    val imagenes: List<String>
)