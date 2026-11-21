package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.Cabana

interface CabanaRepository {
    suspend fun listar(): List<Cabana>
}