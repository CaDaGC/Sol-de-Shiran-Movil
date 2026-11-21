package pe.edu.upeu.solshiran.data.repository

import kotlin.time.Clock
import pe.edu.upeu.solshiran.data.local.AlmacenSesion
import pe.edu.upeu.solshiran.data.mapper.aDominio
import pe.edu.upeu.solshiran.data.remote.AuthApiService
import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val almacen: AlmacenSesion
) : AuthRepository {

    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Sesion {
        val sesion = api.iniciarSesion(
            correo = correo,
            contrasena = contrasena
        ).aDominio()

        check(
            sesion.estaVigente(
                Clock.System.now().epochSeconds
            )
        ) {
            "La sesión recibida ya venció. Revisa la hora del dispositivo."
        }

        almacen.guardar(sesion)

        return sesion
    }

    override suspend fun recuperarSesion(): Sesion? {
        val sesion = almacen.recuperar() ?: return null

        if (!sesion.estaVigente(Clock.System.now().epochSeconds)) {
            almacen.borrar()
            return null
        }

        return sesion
    }

    override suspend fun cerrarSesion() {
        almacen.borrar()
    }
}