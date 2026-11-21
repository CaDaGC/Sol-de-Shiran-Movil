package pe.edu.upeu.solshiran.data.mapper

import kotlin.io.encoding.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import pe.edu.upeu.solshiran.data.remote.dto.LoginResponseDto
import pe.edu.upeu.solshiran.domain.model.Sesion

fun LoginResponseDto.aDominio(): Sesion {
    val partes = token.split(".")

    require(partes.size == 3) {
        "El servidor devolvió un token inválido."
    }

    val contenido = partes[1]
    val contenidoConRelleno = contenido.padEnd(
        ((contenido.length + 3) / 4) * 4,
        '='
    )

    val contenidoJson = Base64.UrlSafe
        .decode(contenidoConRelleno)
        .decodeToString()

    val expiraEn = Json
        .parseToJsonElement(contenidoJson)
        .jsonObject["exp"]
        ?.jsonPrimitive
        ?.longOrNull
        ?: error("El token no contiene una fecha de expiración.")

    return Sesion(
        token = token,
        idUsuario = userId,
        nombreUsuario = nombre,
        correo = email,
        rol = rol.removePrefix("ROLE_"),
        expiraEn = expiraEn
    )
}

fun Sesion.aDto(): LoginResponseDto {
    return LoginResponseDto(
        token = token,
        userId = idUsuario,
        nombre = nombreUsuario,
        email = correo,
        rol = rol
    )
}