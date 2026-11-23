package pe.edu.upeu.solshiran

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pe.edu.upeu.solshiran.data.remote.dto.GestionCabanaRequestDto

class GestionCabanaContractTest {
    @Test
    fun solicitudDeCreacionSigueElContratoDelBackend() {
        val body = Json.encodeToString(
            GestionCabanaRequestDto(
                nombre = "Cabaña Familiar",
                descripcion = "Cabaña con terraza.",
                aforoMaximo = 8,
                numHabitaciones = 3,
                numBanos = 2,
                tarifaNoche = 250.0,
                imagenPrincipal = "/images/cabana-placeholder.svg"
            )
        )

        assertTrue(body.contains("\"numHabitaciones\":3"))
        assertTrue(body.contains("\"numBanos\":2"))
        assertTrue(body.contains("\"imagenPrincipal\":\"/images/cabana-placeholder.svg\""))
        assertTrue(!body.contains("imagenesSecundarias"))
        assertTrue(!body.contains("duenoId"))
        assertEquals(9, GestionCabanaRequestDto.serializer().descriptor.elementsCount)
    }
}
