package pe.edu.upeu.solshiran.data.mapper

import kotlinx.datetime.LocalDate
import pe.edu.upeu.solshiran.data.remote.dto.CotizacionReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.CrearReservaDto
import pe.edu.upeu.solshiran.data.remote.dto.FechasOcupadasDto
import pe.edu.upeu.solshiran.data.remote.dto.ReservaTemporalDto
import pe.edu.upeu.solshiran.domain.model.CotizacionReserva
import pe.edu.upeu.solshiran.domain.model.Estancia
import pe.edu.upeu.solshiran.domain.model.ReservaTemporal

fun FechasOcupadasDto.aDominio() = fechasOcupadas.map { LocalDate.parse(it) }.toSet()

fun CotizacionReservaDto.aDominio() =
    CotizacionReserva(disponible, noches, montoTotal, montoAdelanto, saldoPendiente, mensaje)

fun ReservaTemporalDto.aDominio() =
    ReservaTemporal(idReserva, codigo, expiraEn, montoTotal, montoAdelanto)

fun Estancia.aDto() = CrearReservaDto(
    idCabana, fechaIngreso.toString(), fechaSalida.toString(), cantidadHuespedes,
    horaIngreso, horaSalida
)
