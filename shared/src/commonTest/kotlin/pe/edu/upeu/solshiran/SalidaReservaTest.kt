
package pe.edu.upeu.solshiran

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*
import pe.edu.upeu.solshiran.data.remote.dto.DanosReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.SolicitudSalidaDto
import pe.edu.upeu.solshiran.domain.model.MedioPagoDanos
import pe.edu.upeu.solshiran.domain.model.prepararDanos

class SalidaReservaTest {
    @Test fun salidaSinDanosNoGeneraCobro() {
        assertNull(prepararDanos(false, "", "", MedioPagoDanos.EFECTIVO))
        assertEquals("{}", Json.encodeToString(SolicitudSalidaDto()))
    }

    @Test fun importeAdmiteComaYConservaLaEnie() {
        val danos = prepararDanos(true, " Daño en el baño ", "25,50", MedioPagoDanos.YAPE)!!
        assertEquals("Daño en el baño", danos.descripcion)
        assertEquals(25.5, danos.monto)
        val json = Json.encodeToString(SolicitudSalidaDto(DanosReservaDto(danos.descripcion, danos.monto, danos.medioPago.name)))
        val cuerpo = Json.parseToJsonElement(json).jsonObject.getValue("danos").jsonObject
        assertEquals("YAPE", cuerpo.getValue("medioPago").jsonPrimitive.content)
        assertEquals("Daño en el baño", cuerpo.getValue("descripcion").jsonPrimitive.content)
    }

    @Test fun rechazaImportesNoValidos() {
        for (monto in listOf("0", "-1", "1.234", "NaN", "100000000", "texto")) {
            assertFailsWith<IllegalArgumentException> { prepararDanos(true, "Daño", monto, MedioPagoDanos.EFECTIVO) }
        }
        assertFailsWith<IllegalArgumentException> { prepararDanos(true, " ", "20", MedioPagoDanos.TARJETA) }
        assertFailsWith<IllegalArgumentException> { prepararDanos(true, "x".repeat(501), "20", MedioPagoDanos.PLIN) }
    }
}
