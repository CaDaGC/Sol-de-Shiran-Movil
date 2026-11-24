package pe.edu.upeu.solshiran.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.solshiran.data.remote.dto.CabanaPanelDto
import pe.edu.upeu.solshiran.data.remote.dto.PaginaReservasDto
import pe.edu.upeu.solshiran.data.remote.dto.ReservaDetallePanelDto
import pe.edu.upeu.solshiran.data.remote.dto.ReservaPanelDto
import pe.edu.upeu.solshiran.data.remote.dto.PagoRestanteDto
import pe.edu.upeu.solshiran.domain.model.CabanaPanel
import pe.edu.upeu.solshiran.domain.model.DetalleReservaPanel
import pe.edu.upeu.solshiran.domain.model.EstadoReservaPanel
import pe.edu.upeu.solshiran.domain.model.PaginaReservas
import pe.edu.upeu.solshiran.domain.model.ReservaPanel
import pe.edu.upeu.solshiran.domain.model.PagoRestanteRegistro
import pe.edu.upeu.solshiran.domain.model.MetodoPagoRestante
import pe.edu.upeu.solshiran.domain.model.RegistroSalida

fun CabanaPanelDto.aDominio() = CabanaPanel(idCabana, nombre)

fun PaginaReservasDto.aDominio() =
    PaginaReservas(content.map { it.aDominio() }, page, totalElements, totalPages)

fun ReservaDetallePanelDto.aDominio() = DetalleReservaPanel(
    id = idReserva,
    huesped = nombreUsuario?.takeIf { it.isNotBlank() } ?: "Usuario #$idUsuario",
    correo = emailUsuario?.takeIf { it.isNotBlank() },
    idCabana = idCabana,
    cabana = nombreCabana?.takeIf { it.isNotBlank() },
    ingreso = LocalDate.parse(fechaIngreso),
    salida = LocalDate.parse(fechaSalida),
    cantidadHuespedes = cantidadHuespedes,
    tarifaNoche = tarifaNocheAplicada,
    total = montoTotal,
    adelanto = montoAdelanto,
    saldoPendiente = saldoPendiente,
    estado = EstadoReservaPanel.entries.firstOrNull { it.name == estadoReserva }
        ?: EstadoReservaPanel.DESCONOCIDA,
    expiraEn = expiraEn,
    fechaCreacion = fechaCreacion,
    horaIngreso = horaIngreso?.let { LocalTime.parse(it) },
    horaSalida = horaSalida?.let { LocalTime.parse(it) },
    checkout = checkout?.let {
        RegistroSalida(it.fecha, it.descripcionDanos, it.montoDanos, it.medioPago)
    }
)

fun PagoRestanteDto.aDominio() = PagoRestanteRegistro(
    id = id,
    monto = monto,
    completado = completado,
    metodoPago = MetodoPagoRestante.entries.firstOrNull { it.name == metodoPago } ?: MetodoPagoRestante.EFECTIVO,
    numeroOperacion = numeroOperacion,
    claveIdempotencia = claveIdempotencia
)

private fun ReservaPanelDto.aDominio() = ReservaPanel(
    id = idReserva,
    codigo = codigo?.takeIf { it.isNotBlank() } ?: "Reserva #$idReserva",
    huesped = nombreUsuario?.takeIf { it.isNotBlank() } ?: "Usuario #$idUsuario",
    correo = emailUsuario?.takeIf { it.isNotBlank() },
    cabana = nombreCabana?.takeIf { it.isNotBlank() } ?: "Cabaña #$idCabana",
    ingreso = LocalDate.parse(fechaIngreso),
    salida = LocalDate.parse(fechaSalida),
    horaIngreso = horaIngreso?.let { LocalTime.parse(it) },
    horaSalida = horaSalida?.let { LocalTime.parse(it) },
    cantidadHuespedes = cantidadHuespedes,
    total = montoTotal,
    saldoPendiente = if (estadoReserva == "PENDIENTE_PAGO" || estadoReserva == "EXPIRADA") {
        montoTotal
    } else {
        saldoPendiente
    },
    estado = EstadoReservaPanel.entries.firstOrNull { it.name == estadoReserva }
        ?: EstadoReservaPanel.DESCONOCIDA
)
