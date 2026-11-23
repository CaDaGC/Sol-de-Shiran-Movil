package pe.edu.upeu.solshiran.domain.model

data class GestionCabana(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val aforoMaximo: Int,
    val habitaciones: Int,
    val banos: Int,
    val tarifaNoche: Double,
    val imagenPrincipal: String,
    val estado: String
)

data class FormularioCabana(
    val nombre: String,
    val descripcion: String,
    val aforoMaximo: Int,
    val habitaciones: Int,
    val banos: Int,
    val tarifaNoche: Double,
    val imagenPrincipal: String
)

class ErrorGestionCabanas(mensaje: String, val sesionInvalida: Boolean = false) : Exception(mensaje)
