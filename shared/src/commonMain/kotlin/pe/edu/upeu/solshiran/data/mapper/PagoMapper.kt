package pe.edu.upeu.solshiran.data.mapper

import io.ktor.http.URLProtocol
import io.ktor.http.Url
import pe.edu.upeu.solshiran.data.remote.dto.OrdenAdelantoDto
import pe.edu.upeu.solshiran.data.remote.dto.PagoAdelantoDto
import pe.edu.upeu.solshiran.domain.model.ErrorPago
import pe.edu.upeu.solshiran.domain.model.EstadoAdelanto
import pe.edu.upeu.solshiran.domain.model.OrdenAdelanto
import pe.edu.upeu.solshiran.domain.model.PagoAdelanto

fun OrdenAdelantoDto.aDominio(): OrdenAdelanto {
    val url = runCatching { Url(checkoutUrl) }.getOrNull()
    val dominio = url?.host?.lowercase()
    val dominioPermitido = listOf("mercadopago.com", "mercadopago.com.pe").any {
        dominio == it || dominio?.endsWith(".$it") == true
    }

    if (idTransaccion <= 0 || url == null || !checkoutUrl.startsWith("https://", ignoreCase = true) ||
        url.protocol != URLProtocol.HTTPS || url.port != 443 || !dominioPermitido ||
        url.user != null || url.password != null || checkoutUrl.any { it.isWhitespace() || it == '\\' }
    ) {
        throw ErrorPago("Mercado Pago devolvió una dirección de pago no válida.")
    }
    return OrdenAdelanto(idTransaccion, url.toString())
}

fun PagoAdelantoDto.aDominio(): PagoAdelanto {
    val estadoPago = when (estado) {
        "PENDIENTE" -> EstadoAdelanto.PENDIENTE
        "APROBADA" -> EstadoAdelanto.APROBADA
        "RECHAZADA" -> EstadoAdelanto.RECHAZADA
        else -> throw ErrorPago("El servidor devolvió un estado de pago desconocido.")
    }
    return PagoAdelanto(estadoPago, monto)
}
