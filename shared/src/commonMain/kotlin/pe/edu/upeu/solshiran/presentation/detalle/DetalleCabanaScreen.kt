package pe.edu.upeu.solshiran.presentation.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun DetalleCabanaScreen(
    idCabana: Long,
    onVolverCatalogo: () -> Unit
) {
    val viewModel = koinViewModel<DetalleCabanaViewModel>(
        key = "detalle_$idCabana",
        parameters = {
            parametersOf(idCabana)
        }
    )

    val estado by viewModel.estado.collectAsStateWithLifecycle()

    when {
        estado.cargando -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        estado.mensajeError != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(
                    space = 16.dp,
                    alignment = Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = estado.mensajeError.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = viewModel::cargarDetalle
                ) {
                    Text("Reintentar")
                }

                TextButton(
                    onClick = onVolverCatalogo
                ) {
                    Text("Volver al catálogo")
                }
            }
        }

        else -> {
            val cabana = estado.cabana

            if (cabana != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GaleriaCabana(
                        imagenes = cabana.imagenes,
                        nombre = cabana.nombre
                    )

                    Text(
                        text = cabana.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${formatearSoles(cabana.tarifaNoche)} por noche",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Características",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Capacidad máxima: ${cabana.aforoMaximo} personas")
                    Text("Habitaciones: ${cabana.numeroHabitaciones}")
                    Text("Baños: ${cabana.numeroBanos}")

                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = cabana.descripcion.ifBlank {
                            "Esta cabaña todavía no tiene una descripción."
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    TextButton(
                        onClick = viewModel::cargarDetalle
                    ) {
                        Text("Actualizar información")
                    }

                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reservar ahora")
                    }
                }
            }
        }
    }
}

@Composable
private fun GaleriaCabana(
    imagenes: List<String>,
    nombre: String
) {
    if (imagenes.isEmpty()) {
        ImagenSinContenido()
        return
    }

    val pagina = rememberPagerState(
        pageCount = {
            imagenes.size
        }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalPager(
            state = pagina,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) { indice ->
            SubcomposeAsyncImage(
                model = imagenes[indice],
                contentDescription =
                    "Foto ${indice + 1} de $nombre",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    ImagenSinContenido()
                }
            )
        }

        Text(
            text = "Foto ${pagina.currentPage + 1} de ${imagenes.size}",
            style = MaterialTheme.typography.bodySmall
        )

        if (imagenes.size > 1) {
            Text(
                text = "Desliza para ver más fotografías.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ImagenSinContenido() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("Imagen no disponible")
    }
}