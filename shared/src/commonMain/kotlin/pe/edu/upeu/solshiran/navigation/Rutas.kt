package pe.edu.upeu.solshiran.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Catalogo

@Serializable
data object Login

@Serializable
data object Cuenta

@Serializable
data object PanelReservas

@Serializable
data object GestionCabanas

@Serializable
data class DetalleCabana(
    val idCabana: Long
)

@Serializable
data class ReservarCabana(val idCabana: Long)
