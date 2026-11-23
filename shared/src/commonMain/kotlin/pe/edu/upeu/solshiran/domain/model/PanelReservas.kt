package pe.edu.upeu.solshiran.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class CabanaPanel(val id: Long, val nombre: String)

data class FiltroPanelReservas(
    val buscar: String = "",
    val idCabana: Long? = null,
    val pagina: Int = 0
)

enum class EstadoReservaPanel {
    PENDIENTE_PAGO, CONFIRMADA, OCUPADA, FINALIZADA, EXPIRADA, CANCELADA, DESCONOCIDA
}

data class ReservaPanel(
    val id: Long,
    val codigo: String,
    val huesped: String,
    val correo: String?,
    val cabana: String,
    val ingreso: LocalDate,
    val salida: LocalDate,
    val horaIngreso: LocalTime?,
    val horaSalida: LocalTime?,
    val cantidadHuespedes: Int,
    val total: Double,
    val saldoPendiente: Double,
    val estado: EstadoReservaPanel
)

data class PaginaReservas(
    val reservas: List<ReservaPanel>,
    val pagina: Int,
    val totalReservas: Long,
    val totalPaginas: Int
)

fun Sesion?.puedeConsultarPanelReservas(): Boolean =
    this?.rol == "TRABAJADOR" || this?.rol == "ADMIN"
