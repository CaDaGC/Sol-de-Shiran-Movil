package pe.edu.upeu.solshiran.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

enum class GrupoMisReservas(val etiqueta: String) {
    TODAS("Todas"), PENDIENTES("Pendientes"), CONFIRMADAS("Activas"), PASADAS("Historial")
}

data class MiReserva(
    val id: Long,
    val cabana: String,
    val ingreso: LocalDate,
    val salida: LocalDate,
    val huespedes: Int,
    val total: Double,
    val adelanto: Double,
    val saldo: Double,
    val tarifa: Double,
    val estado: EstadoReservaPanel,
    val horaIngreso: LocalTime? = null,
    val horaSalida: LocalTime? = null,
    val checkout: RegistroSalida? = null
)

data class PaginaMisReservas(val reservas: List<MiReserva>, val pagina: Int, val totalPaginas: Int)
