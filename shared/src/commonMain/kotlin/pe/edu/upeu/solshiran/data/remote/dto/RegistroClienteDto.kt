package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonaRegistroDto(
    val nombres: String,
    val apellidos: String,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val telefono: String,
    val direccion: String
)

@Serializable
data class RegistroClienteDto(
    val nombreUsuario: String,
    val email: String,
    val password: String,
    val persona: PersonaRegistroDto
)
