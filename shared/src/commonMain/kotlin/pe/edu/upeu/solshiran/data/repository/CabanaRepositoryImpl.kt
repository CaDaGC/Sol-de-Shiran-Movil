package pe.edu.upeu.solshiran.data.repository

import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.CatalogoApiService
import pe.edu.upeu.solshiran.data.remote.ConfiguracionApi
import pe.edu.upeu.solshiran.domain.model.Cabana
import pe.edu.upeu.solshiran.domain.model.ErrorCatalogo
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.CabanaRepository

class CabanaRepositoryImpl(
    private val api: CatalogoApiService,
    private val authRepository: AuthRepository,
    private val configuracion: ConfiguracionApi
) : CabanaRepository {

    override suspend fun listar(): List<Cabana> {
        val sesion = authRepository.recuperarSesion()

        if (
            sesion != null &&
            sesion.rol !in setOf("CLIENTE", "ADMIN")
        ) {
            throw ErrorCatalogo(
                "Tu cuenta no tiene acceso al catálogo."
            )
        }

        return api.listar(
            token = sesion?.token
        ).map { dto ->
            dto.aDominio(
                urlBase = configuracion.urlBase
            )
        }
    }
}