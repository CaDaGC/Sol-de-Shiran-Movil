package pe.edu.upeu.solshiran

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.dto.ReservaDetallePanelDto
import pe.edu.upeu.solshiran.domain.model.EstadoReservaPanel

class DetalleReservaPanelTest {
    @Test
    fun convierteRespuestaRealDeDetalleAlDominio() {
        val detalle = ReservaDetallePanelDto(
            idReserva = 81,
            idUsuario = 12,
            nombreUsuario = "Ana Pérez",
            emailUsuario = "ana@example.test",
            idCabana = 5,
            nombreCabana = "Cabaña Sol",
            fechaIngreso = "2026-11-25",
            fechaSalida = "2026-11-27",
            cantidadHuespedes = 4,
            tarifaNocheAplicada = 150.0,
            montoTotal = 300.0,
            montoAdelanto = 75.0,
            saldoPendiente = 225.0,
            estadoReserva = "OCUPADA",
            expiraEn = null,
            fechaCreacion = "2026-11-01T10:30:00"
        ).aDominio()

        assertEquals(81L, detalle.id)
        assertEquals("Ana Pérez", detalle.huesped)
        assertEquals("Cabaña Sol", detalle.cabana)
        assertEquals(EstadoReservaPanel.OCUPADA, detalle.estado)
        assertEquals(4, detalle.cantidadHuespedes)
        assertEquals(300.0, detalle.total)
        assertNull(detalle.expiraEn)
        assertEquals("2026-11-01T10:30:00", detalle.fechaCreacion)
    }
}
