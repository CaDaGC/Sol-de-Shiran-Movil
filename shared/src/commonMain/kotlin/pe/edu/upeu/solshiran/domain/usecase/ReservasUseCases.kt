package pe.edu.upeu.solshiran.domain.usecase

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pe.edu.upeu.solshiran.domain.model.ErrorReserva
import pe.edu.upeu.solshiran.domain.model.Estancia
import pe.edu.upeu.solshiran.domain.repository.ReservaRepository

internal fun hoyEnLima(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.of("America/Lima")).date

class ConsultarFechasOcupadasUseCase(private val repository: ReservaRepository) {
    suspend operator fun invoke(idCabana: Long, desde: LocalDate, hasta: LocalDate) =
        repository.fechasOcupadas(idCabana, desde, hasta)
}

class ConsultarDisponibilidadUseCase(private val repository: ReservaRepository) {
    suspend operator fun invoke(estancia: Estancia, aforo: Int) =
        repository.cotizar(validarEstancia(estancia, aforo))
}

class RegistrarReservaUseCase(private val repository: ReservaRepository) {
    suspend operator fun invoke(estancia: Estancia, aforo: Int) =
        repository.registrar(validarEstancia(estancia, aforo))
}

private fun validarEstancia(estancia: Estancia, aforo: Int): Estancia {
    when {
        estancia.fechaIngreso < hoyEnLima() ->
            throw ErrorReserva("Elige una fecha de ingreso desde hoy.")
        estancia.fechaSalida <= estancia.fechaIngreso ->
            throw ErrorReserva("La salida debe ser posterior al ingreso.")
        estancia.cantidadHuespedes !in 1..aforo ->
            throw ErrorReserva("Ingresa entre 1 y $aforo huéspedes.")
    }
    return estancia
}
