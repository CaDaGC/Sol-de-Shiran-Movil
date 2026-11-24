package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.GrupoMisReservas
import pe.edu.upeu.solshiran.domain.model.MiReserva
import pe.edu.upeu.solshiran.domain.model.PaginaMisReservas

interface MisReservasRepository {
    suspend fun consultar(grupo: GrupoMisReservas, pagina: Int): PaginaMisReservas
    suspend fun detalle(idReserva: Long): MiReserva
}
