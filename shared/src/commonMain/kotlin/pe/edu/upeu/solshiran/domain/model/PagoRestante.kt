package pe.edu.upeu.solshiran.domain.model

enum class MetodoPagoRestante(val etiqueta: String) {
    EFECTIVO("Efectivo"), TARJETA("Tarjeta"), YAPE_BILLETERA("Yape/Billetera")
}

data class PagoRestanteRegistro(
    val id: Long,
    val monto: Double,
    val completado: Boolean,
    val metodoPago: MetodoPagoRestante,
    val numeroOperacion: String?,
    val claveIdempotencia: String
)

data class SolicitudPagoRestante(
    val metodoPago: MetodoPagoRestante,
    val numeroOperacion: String?,
    val claveIdempotencia: String
)
