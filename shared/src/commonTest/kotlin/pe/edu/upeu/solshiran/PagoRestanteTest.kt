package pe.edu.upeu.solshiran

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.dto.PagoRestanteDto
import pe.edu.upeu.solshiran.data.remote.dto.SolicitudPagoRestanteDto

class PagoRestanteTest {
    @Test
    fun solicitudRespetaContratoYNoEnvíaImporte() {
        val solicitud = SolicitudPagoRestanteDto(
            metodoPago = "YAPE_BILLETERA",
            numeroOperacion = "OP-12345",
            claveIdempotencia = "123e4567-e89b-12d3-a456-426614174000"
        )

        val json = Json.encodeToString(solicitud)

        assertTrue(json.contains("\"metodoPago\":\"YAPE_BILLETERA\""))
        assertTrue(json.contains("\"numeroOperacion\":\"OP-12345\""))
        assertTrue(json.contains("\"claveIdempotencia\":\"123e4567-e89b-12d3-a456-426614174000\""))
        assertFalse(json.contains("monto"))
    }

    @Test
    fun respuestaConservaMetodoMontoYClaveParaReintentos() {
        val registro = PagoRestanteDto(
            id = 8,
            idReserva = 31,
            claveIdempotencia = "123e4567-e89b-12d3-a456-426614174000",
            monto = 225.0,
            metodoPago = "TARJETA",
            numeroOperacion = "T-99",
            completado = true,
            fechaRegistro = "2026-11-23T12:00:00"
        ).aDominio()

        assertEquals(225.0, registro.monto)
        assertEquals("TARJETA", registro.metodoPago.name)
        assertEquals("T-99", registro.numeroOperacion)
        assertEquals("123e4567-e89b-12d3-a456-426614174000", registro.claveIdempotencia)
        assertTrue(registro.completado)
    }
}
