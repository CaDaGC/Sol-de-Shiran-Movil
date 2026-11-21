package pe.edu.upeu.solshiran.presentation.catalogo

import pe.edu.upeu.solshiran.domain.model.Cabana

data class CatalogoUiState(
    val cabanas: List<Cabana> = emptyList(),
    val busqueda: String = "",
    val cargando: Boolean = true,
    val mensajeError: String? = null
) {
    val cabanasVisibles: List<Cabana>
        get() {
            val texto = busqueda.trim()

            return if (texto.isEmpty()) {
                cabanas
            } else {
                cabanas.filter {
                    it.nombre.contains(
                        other = texto,
                        ignoreCase = true
                    )
                }
            }
        }
}