package pe.edu.upeu.solshiran.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Catalogo

@Serializable
data object Login

@Serializable
data object Cuenta

@Serializable
data class DetalleCabana(
    val idCabana: Long
)