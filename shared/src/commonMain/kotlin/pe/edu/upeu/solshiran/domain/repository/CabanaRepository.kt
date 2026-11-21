package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.Cabana
import pe.edu.upeu.solshiran.domain.model.CabanaDetalle

interface CabanaRepository {
    suspend fun listar(): List<Cabana>

    suspend fun obtenerDetalle(idCabana: Long): CabanaDetalle
}