package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.CabanaPanel
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.model.PaginaReservas

interface PanelReservasRepository {
    suspend fun listarCabanas(): List<CabanaPanel>
    suspend fun consultar(filtro: FiltroPanelReservas): PaginaReservas
}
