package pe.edu.upeu.solshiran.data.repository

import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.CatalogoApiService
import pe.edu.upeu.solshiran.data.remote.ConfiguracionApi
import pe.edu.upeu.solshiran.domain.model.Cabana
import pe.edu.upeu.solshiran.domain.model.CabanaDetalle
import pe.edu.upeu.solshiran.domain.model.ErrorCatalogo
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.CabanaRepository

class CabanaRepositoryImpl(
    private val api: CatalogoApiService,
    private val authRepository: AuthRepository,
    private val configuracion: ConfiguracionApi
) : CabanaRepository {

    override suspend fun listar(): List<Cabana> {
        val token = obtenerTokenPermitido()

        return api.listar(token).map { dto ->
            dto.aDominio(
                urlBase = configuracion.urlBase
            )
        }
    }

    override suspend fun obtenerDetalle(
        idCabana: Long
    ): CabanaDetalle {
        val token = obtenerTokenPermitido()

        return api.obtenerDetalle(
            idCabana = idCabana,
            token = token
        ).aDominio(
            urlBase = configuracion.urlBase
        )
    }

    private suspend fun obtenerTokenPermitido(): String? {
        val sesion = authRepository.recuperarSesion()

        if (
            sesion != null &&
            sesion.rol !in setOf("CLIENTE", "ADMIN")
        ) {
            throw ErrorCatalogo(
                "Tu cuenta no tiene acceso al catálogo."
            )
        }

        return sesion?.token
    }
}