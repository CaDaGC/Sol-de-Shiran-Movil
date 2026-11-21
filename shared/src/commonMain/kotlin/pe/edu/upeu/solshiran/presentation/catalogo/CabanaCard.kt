package pe.edu.upeu.solshiran.presentation.catalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import pe.edu.upeu.solshiran.domain.model.Cabana

@Composable
fun CabanaCard(
    cabana: Cabana,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        val imagen = cabana.imagenPrincipal

        if (imagen == null) {
            ImagenNoDisponible()
        } else {
            SubcomposeAsyncImage(
                model = imagen,
                contentDescription = "Fotografía de ${cabana.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    ImagenNoDisponible()
                }
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = cabana.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (cabana.descripcion.isNotBlank()) {
                Text(
                    text = cabana.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Hasta ${cabana.aforoMaximo} personas",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${cabana.numeroHabitaciones} habitaciones · " +
                        "${cabana.numeroBanos} baños",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${formatearSoles(cabana.tarifaNoche)} por noche",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ImagenNoDisponible() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Imagen no disponible",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}