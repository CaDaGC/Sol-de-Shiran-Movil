package pe.edu.upeu.solshiran.domain.usecase

import pe.edu.upeu.solshiran.domain.model.GrupoMisReservas
import pe.edu.upeu.solshiran.domain.repository.MisReservasRepository

class ConsultarMisReservasUseCase(private val repository: MisReservasRepository) {
    suspend operator fun invoke(grupo: GrupoMisReservas, pagina: Int) = repository.consultar(grupo, pagina)
    suspend fun detalle(idReserva: Long) = repository.detalle(idReserva)
}
