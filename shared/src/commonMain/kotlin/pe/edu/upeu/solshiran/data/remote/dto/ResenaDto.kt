package pe.edu.upeu.solshiran.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResenaDto(
    val id: Long,
    val autor: String?,
    val puntuacion: Int,
    val comentario: String,
    val fecha: String?
)

@Serializable
data class PaginaResenasDto(
    val opiniones: List<ResenaDto>,
    val hayMas: Boolean
)

@Serializable
data class ElegibilidadResenaDto(val idReserva: Long?)

@Serializable
data class PublicarResenaDto(
    val idReserva: Long,
    val puntuacion: Int,
    val comentario: String
)
