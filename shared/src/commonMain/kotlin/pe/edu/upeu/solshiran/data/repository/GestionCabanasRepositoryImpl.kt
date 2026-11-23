package pe.edu.upeu.solshiran.data.repository

import pe.edu.upeu.solshiran.data.remote.GestionCabanasApiService
import pe.edu.upeu.solshiran.data.remote.dto.GestionCabanaRequestDto
import pe.edu.upeu.solshiran.domain.model.ErrorGestionCabanas
import pe.edu.upeu.solshiran.domain.model.FormularioCabana
import pe.edu.upeu.solshiran.domain.model.GestionCabana
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.GestionCabanasRepository

class GestionCabanasRepositoryImpl(
    private val api: GestionCabanasApiService,
    private val auth: AuthRepository
) : GestionCabanasRepository {
    override suspend fun listar() = api.listar(token()).map { it.toDominio() }

    override suspend fun guardar(id: Long?, formulario: FormularioCabana): GestionCabana {
        val token = token()
        val request = GestionCabanaRequestDto(
            nombre = formulario.nombre.trim(),
            descripcion = formulario.descripcion.trim(),
            aforoMaximo = formulario.aforoMaximo,
            numHabitaciones = formulario.habitaciones,
            numBanos = formulario.banos,
            tarifaNoche = formulario.tarifaNoche,
            imagenPrincipal = formulario.imagenPrincipal.trim()
        )
        return (if (id == null) api.crear(token, request) else api.editar(id, token, request)).toDominio()
    }

    override suspend fun desactivar(id: Long) = api.desactivar(id, token())
    override suspend fun activar(id: Long) = api.activar(id, token()).toDominio()

    private suspend fun token(): String {
        val sesion = auth.recuperarSesion() ?: throw ErrorGestionCabanas(
            "Tu sesión expiró. Vuelve a iniciar sesión.", sesionInvalida = true
        )
        if (sesion.rol != "ADMIN") throw ErrorGestionCabanas("Solo una cuenta administradora puede gestionar cabañas.")
        return sesion.token
    }
}

private fun pe.edu.upeu.solshiran.data.remote.dto.GestionCabanaDto.toDominio() = GestionCabana(
    id = idCabana,
    nombre = nombre,
    descripcion = descripcion,
    aforoMaximo = aforoMaximo,
    habitaciones = numHabitaciones,
    banos = numBanos,
    tarifaNoche = tarifaNoche,
    imagenPrincipal = imagenPrincipal,
    estado = estado
)
