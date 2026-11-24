package pe.edu.upeu.solshiran.domain.model

enum class MedioPagoDanos(val etiqueta: String) {
    EFECTIVO("Efectivo"), TARJETA("Tarjeta"), YAPE("Yape"), PLIN("Plin")
}

data class DanosReserva(val descripcion: String, val monto: Double, val medioPago: MedioPagoDanos)
data class RegistroSalida(val fecha: String, val descripcion: String?, val monto: Double?, val medioPago: String?)

fun prepararDanos(hayDanos: Boolean, descripcion: String, importe: String, medio: MedioPagoDanos): DanosReserva? {
    if (!hayDanos) return null
    val detalle = descripcion.trim()
    require(detalle.isNotEmpty() && detalle.length <= 500) { "Describe los daños en un máximo de 500 caracteres." }
    val texto = importe.trim().replace(',', '.')
    require(texto.matches(Regex("[0-9]{1,8}(\\.[0-9]{1,2})?"))) { "Ingresa un importe válido con hasta dos decimales." }
    val monto = texto.toDouble()
    require(monto > 0) { "El importe de los daños debe ser mayor a cero." }
    return DanosReserva(detalle, monto, medio)
}
