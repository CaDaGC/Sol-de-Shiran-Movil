package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.CabanaPanel
import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.model.PaginaReservas
import pe.edu.upeu.solshiran.domain.model.PagoRestanteRegistro
import pe.edu.upeu.solshiran.domain.model.SolicitudPagoRestante

interface PanelReservasRepository {
    suspend fun listarCabanas(): List<CabanaPanel>
    suspend fun consultar(filtro: FiltroPanelReservas): PaginaReservas
    suspend fun detalle(idReserva: Long): DetalleReservaPanel
    suspend fun consultarPagoRestante(idReserva: Long): PagoRestanteRegistro?
    suspend fun registrarPagoRestante(idReserva: Long, solicitud: SolicitudPagoRestante): PagoRestanteRegistro
}
