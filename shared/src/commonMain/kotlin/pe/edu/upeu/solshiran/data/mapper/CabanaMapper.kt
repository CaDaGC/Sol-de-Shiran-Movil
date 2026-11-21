package pe.edu.upeu.solshiran.data.mapper

import io.ktor.http.Url
import pe.edu.upeu.solshiran.data.remote.dto.CabanaCatalogoDto
import pe.edu.upeu.solshiran.domain.model.Cabana

fun CabanaCatalogoDto.aDominio(
    urlBase: String
): Cabana {
    return Cabana(
        id = idCabana,
        nombre = nombre,
        descripcion = descripcion.orEmpty(),
        aforoMaximo = aforoMaximo,
        numeroHabitaciones = numHabitaciones,
        numeroBanos = numBanos,
        tarifaNoche = tarifaNoche,
        imagenPrincipal = resolverImagen(
            ruta = imagenPrincipal,
            urlBase = urlBase
        )
    )
}

private fun resolverImagen(
    ruta: String?,
    urlBase: String
): String? {
    val imagen = ruta?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return null

    if (imagen.startsWith("/images/")) {
        return null
    }

    if (
        imagen.startsWith("https://") ||
        imagen.startsWith("http://")
    ) {
        return imagen
    }

    if (!imagen.startsWith("/api/")) {
        return null
    }

    val base = Url(urlBase)

    val origen = buildString {
        append(base.protocol.name)
        append("://")
        append(base.host)
        append(":")
        append(base.port)
    }

    return origen + imagen
}