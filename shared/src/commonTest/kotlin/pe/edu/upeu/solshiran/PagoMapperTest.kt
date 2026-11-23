package pe.edu.upeu.solshiran

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.dto.OrdenAdelantoDto
import pe.edu.upeu.solshiran.data.remote.dto.PagoAdelantoDto
import pe.edu.upeu.solshiran.domain.model.ErrorPago
import pe.edu.upeu.solshiran.domain.model.EstadoAdelanto

class PagoMapperTest {
    @Test
    fun aceptaCheckoutHttpsDeMercadoPago() {
        val url = "https://www.mercadopago.com.pe/checkout?orden=ORD123"
        assertEquals(url, OrdenAdelantoDto(42, url).aDominio().url)
    }

    @Test
    fun rechazaDireccionesFueraDeLaPasarela() {
        val urls = listOf(
            "http://www.mercadopago.com.pe/checkout",
            "https://mercadopago.com.pe.ejemplo.com/checkout",
            "https://mercadopago.com.pe@ejemplo.com/checkout",
            "https://ejemplo@mercadopago.com.pe/checkout",
            "https://mercadopago.com.pe:444/checkout"
        )
        urls.forEach { url ->
            assertFailsWith<ErrorPago> { OrdenAdelantoDto(42, url).aDominio() }
        }
    }

    @Test
    fun soloAceptaLosEstadosDelContrato() {
        assertEquals(EstadoAdelanto.APROBADA, PagoAdelantoDto(42, 3, "APROBADA", 100.0).aDominio().estado)
        assertFailsWith<ErrorPago> { PagoAdelantoDto(42, 3, "SUCCESS", 100.0).aDominio() }
    }
}
