package pe.edu.upeu.solshiran.data.mapper

import io.ktor.http.Url
import pe.edu.upeu.solshiran.data.remote.dto.CabanaCatalogoDto
import pe.edu.upeu.solshiran.data.remote.dto.CabanaDetalleDto
import pe.edu.upeu.solshiran.domain.model.Cabana
import pe.edu.upeu.solshiran.domain.model.CabanaDetalle

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

fun CabanaDetalleDto.aDominio(
    urlBase: String
): CabanaDetalle {
    val todasLasImagenes = (
            listOfNotNull(imagenPrincipal) + imagenesSecundarias
            ).mapNotNull { ruta ->
            resolverImagen(
                ruta = ruta,
                urlBase = urlBase
            )
        }.distinct()

    return CabanaDetalle(
        id = idCabana,
        nombre = nombre,
        descripcion = descripcion.orEmpty(),
        aforoMaximo = aforoMaximo,
        numeroHabitaciones = numHabitaciones,
        numeroBanos = numBanos,
        tarifaNoche = tarifaNoche,
        imagenes = todasLasImagenes
    )
}

private fun resolverImagen(ruta: String?, urlBase: String): String? {
    val imagen = ruta?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    val esUrlAbsoluta = imagen.startsWith("http://", ignoreCase = true) ||
            imagen.startsWith("https://", ignoreCase = true)

    val rutaGateway = if (esUrlAbsoluta) {
        val url = runCatching { Url(imagen) }.getOrNull() ?: return null

        if (!url.encodedPath.startsWith("/api/v1/cabanas/fotos/")) {
            return imagen
        }

        url.encodedPath
    } else {
        if (!imagen.startsWith("/api/")) return null
        imagen
    }

    val base = Url(urlBase)
    val puerto = if (base.port == base.protocol.defaultPort) "" else ":${base.port}"
    return "${base.protocol.name}://${base.host}$puerto$rutaGateway"
}