package pe.edu.upeu.solshiran.data.local

import pe.edu.upeu.solshiran.domain.model.Sesion

interface AlmacenSesion {
    suspend fun guardar(sesion: Sesion)

    suspend fun recuperar(): Sesion?

    suspend fun borrar()
}