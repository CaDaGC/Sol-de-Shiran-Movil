package pe.edu.upeu.solshiran.presentation.detalle

import pe.edu.upeu.solshiran.domain.model.CabanaDetalle

data class DetalleCabanaUiState(
    val cargando: Boolean = true,
    val cabana: CabanaDetalle? = null,
    val mensajeError: String? = null
)