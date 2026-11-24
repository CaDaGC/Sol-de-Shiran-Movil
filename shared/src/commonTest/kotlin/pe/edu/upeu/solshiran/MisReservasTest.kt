
package pe.edu.upeu.solshiran

import kotlinx.serialization.json.Json
import kotlin.test.*
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.dto.MiReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.PaginaMisReservasDto
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.EstadoReservaPanel

class MisReservasTest {
    private val respuesta = """{"content":[{"idReserva":12,"idUsuario":7,"idCabana":3,"nombreCabana":"Cabaña Sol","fechaIngreso":"2026-12-05","horaIngreso":"14:00:00","fechaSalida":"2026-12-06","horaSalida":"14:00:00","cantidadHuespedes":4,"tarifaNocheAplicada":600,"montoTotal":600,"montoAdelanto":300,"saldoPendiente":300,"estadoReserva":"CONFIRMADA","grupo":"CONFIRMADAS","pagoDisponible":false}],"page":0,"size":20,"totalElements":1,"totalPages":1}"""

    @Test fun historialConservaHorasImportesYTextoEspanol() {
        val pagina = Json { ignoreUnknownKeys = true }.decodeFromString<PaginaMisReservasDto>(respuesta)
        val reserva = pagina.content.single().aDominio(7)
        assertEquals("Cabaña Sol", reserva.cabana)
        assertEquals("14:00", reserva.horaIngreso.toString())
        assertEquals(600.0, reserva.total)
        assertEquals(EstadoReservaPanel.CONFIRMADA, reserva.estado)
    }

    @Test fun impideMostrarUnaReservaDeOtroCliente() {
        val reserva = Json { ignoreUnknownKeys = true }.decodeFromString<PaginaMisReservasDto>(respuesta).content.single()
        assertFailsWith<ErrorReserva> { reserva.aDominio(99) }
    }

    @Test fun detalleFinalizadoConservaLosDanos() {
        val reserva = Json.decodeFromString<MiReservaDto>("""{"idReserva":12,"idUsuario":7,"idCabana":3,"fechaIngreso":"2026-12-05","fechaSalida":"2026-12-06","cantidadHuespedes":4,"tarifaNocheAplicada":600,"montoTotal":600,"montoAdelanto":300,"saldoPendiente":0,"estadoReserva":"FINALIZADA","checkout":{"fecha":"2026-12-06T14:00:00","descripcionDanos":"Daño en silla","montoDanos":25.5,"medioPago":"EFECTIVO"}}""").aDominio(7)
        assertEquals("Cabaña #3", reserva.cabana)
        assertEquals(25.5, reserva.checkout?.monto)
        assertEquals(EstadoReservaPanel.FINALIZADA, reserva.estado)
    }
}
