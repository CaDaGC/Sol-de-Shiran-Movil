package pe.edu.upeu.solshiran.domain.model

data class OrdenAdelanto(val idTransaccion: Long, val url: String)

enum class EstadoAdelanto { PENDIENTE, APROBADA, RECHAZADA }

data class PagoAdelanto(
    val estado: EstadoAdelanto,
    val monto: Double
)

class ErrorPago(
    mensaje: String,
    val sesionInvalida: Boolean = false
) : Exception(mensaje)
