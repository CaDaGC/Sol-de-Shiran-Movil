package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val token: String,
    val userId: Long,
    val nombre: String,
    val email: String,
    val rol: String
)