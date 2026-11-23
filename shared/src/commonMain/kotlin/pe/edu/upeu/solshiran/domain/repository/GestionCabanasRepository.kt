package pe.edu.upeu.solshiran.domain.repository

import pe.edu.upeu.solshiran.domain.model.FormularioCabana
import pe.edu.upeu.solshiran.domain.model.GestionCabana

interface GestionCabanasRepository {
    suspend fun listar(): List<GestionCabana>
    suspend fun guardar(id: Long?, formulario: FormularioCabana): GestionCabana
    suspend fun desactivar(id: Long)
    suspend fun activar(id: Long): GestionCabana
}
