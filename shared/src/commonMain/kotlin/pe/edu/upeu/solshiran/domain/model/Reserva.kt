package pe.edu.upeu.solshiran.domain.model

import kotlinx.datetime.LocalDate

data class Estancia(
    val idCabana: Long,
    val fechaIngreso: LocalDate,
    val fechaSalida: LocalDate,
    val cantidadHuespedes: Int
) {
    val horaIngreso: String get() = "11:00"
    val horaSalida: String get() = "13:00"
}

data class CotizacionReserva(
    val disponible: Boolean,
    val noches: Long,
    val total: Double,
    val adelanto: Double,
    val saldo: Double,
    val mensaje: String?
)

data class ReservaTemporal(
    val id: Long,
    val codigo: String?,
    val expiraEn: String,
    val total: Double
)

class ErrorReserva(
    mensaje: String,
    val sesionInvalida: Boolean = false
) : Exception(mensaje)
