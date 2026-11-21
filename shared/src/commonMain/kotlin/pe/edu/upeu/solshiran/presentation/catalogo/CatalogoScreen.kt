package pe.edu.upeu.solshiran.presentation.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CatalogoScreen(
    onVerDetalle: (Long) -> Unit
) {
    val viewModel = koinViewModel<CatalogoViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.cargarCatalogo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nuestras cabañas",
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(
                onClick = viewModel::cargarCatalogo,
                enabled = !estado.cargando
            ) {
                Text("Actualizar")
            }
        }

        OutlinedTextField(
            value = estado.busqueda,
            onValueChange = viewModel::cambiarBusqueda,
            label = {
                Text("Buscar por nombre")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        estado.mensajeError?.let { mensaje ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = viewModel::cargarCatalogo,
                    enabled = !estado.cargando
                ) {
                    Text("Reintentar")
                }
            }
        }

        if (estado.cargando && estado.cabanas.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        val visibles = estado.cabanasVisibles

        when {
            estado.cargando && estado.cabanas.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            estado.cabanas.isEmpty() &&
                    estado.mensajeError == null -> {
                MensajeCatalogo(
                    "Todavía no hay cabañas para mostrar."
                )
            }

            estado.cabanas.isNotEmpty() &&
                    visibles.isEmpty() -> {
                MensajeCatalogo(
                    "No encontramos cabañas con ese nombre."
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = visibles,
                        key = { it.id }
                    ) { cabana ->
                        CabanaCard(
                            cabana = cabana,
                            onVerDetalle = {
                                onVerDetalle(cabana.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeCatalogo(
    texto: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto)
    }
}