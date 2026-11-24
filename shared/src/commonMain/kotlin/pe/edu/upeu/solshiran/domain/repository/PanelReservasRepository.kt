package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.CabanaPanel
import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.FiltroPanelReservas
import pe.edu.upeu.solshiran.domain.model.PaginaReservas
import pe.edu.upeu.solshiran.domain.model.PagoRestanteRegistro
import pe.edu.upeu.solshiran.domain.model.SolicitudPagoRestante
import pe.edu.upeu.solshiran.domain.model.DanosReserva
import pe.edu.upeu.solshiran.domain.model.RegistroSalida

interface PanelReservasRepository {
    suspend fun registrarSalida(idReserva: Long, danos: DanosReserva?): RegistroSalida
    suspend fun listarCabanas(): List<CabanaPanel>
    suspend fun consultar(filtro: FiltroPanelReservas): PaginaReservas
    suspend fun detalle(idReserva: Long): DetalleReservaPanel
    suspend fun consultarPagoRestante(idReserva: Long): PagoRestanteRegistro?
    suspend fun registrarPagoRestante(idReserva: Long, solicitud: SolicitudPagoRestante): PagoRestanteRegistro
}
