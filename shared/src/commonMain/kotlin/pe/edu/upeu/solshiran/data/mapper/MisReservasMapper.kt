package pe.edu.upeu.solshiran.data.mapper

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import pe.edu.upeu.solshiran.data.remote.dto.MiReservaDto
import pe.edu.upeu.solshiran.domain.model.*

fun MiReservaDto.aDominio(idCliente: Long): MiReserva {
    if (idUsuario != idCliente) throw ErrorReserva("La reserva no pertenece a tu cuenta.")
    return MiReserva(
        id = idReserva,
        cabana = nombreCabana?.takeIf { it.isNotBlank() } ?: "Cabaña #$idCabana",
        ingreso = LocalDate.parse(fechaIngreso),
        salida = LocalDate.parse(fechaSalida),
        huespedes = cantidadHuespedes,
        total = montoTotal,
        adelanto = montoAdelanto,
        saldo = saldoPendiente,
        tarifa = tarifaNocheAplicada,
        estado = EstadoReservaPanel.entries.firstOrNull { it.name == estadoReserva } ?: EstadoReservaPanel.DESCONOCIDA,
        horaIngreso = horaIngreso?.let { LocalTime.parse(it) },
        horaSalida = horaSalida?.let { LocalTime.parse(it) },
        checkout = checkout?.let { RegistroSalida(it.fecha, it.descripcionDanos, it.montoDanos, it.medioPago) }
    )
}
