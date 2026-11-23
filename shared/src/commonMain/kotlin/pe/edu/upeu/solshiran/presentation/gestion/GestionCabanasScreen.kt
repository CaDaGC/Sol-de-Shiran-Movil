package pe.edu.upeu.solshiran.presentation.gestion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.domain.model.GestionCabana
import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun GestionCabanasScreen(sesion: Sesion?, onSesionInvalida: () -> Unit) {
    val viewModel = koinViewModel<GestionCabanasViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var cabanaEnEdicion by remember { mutableStateOf<GestionCabana?>(null) }
    var crearAbierto by rememberSaveable { mutableStateOf(false) }
    var cabanaPorDesactivar by remember { mutableStateOf<GestionCabana?>(null) }

    LaunchedEffect(estado.sesionInvalida) {
        if (estado.sesionInvalida) onSesionInvalida()
    }
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            crearAbierto = false
            viewModel.limpiarGuardado()
        }
    }

    if (sesion?.rol != "ADMIN") {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Acceso restringido", style = MaterialTheme.typography.titleLarge)
            Text("Solo una cuenta administradora puede gestionar cabañas.")
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Gestión de cabañas", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = viewModel::cargar, enabled = !estado.cargando) { Text("Actualizar") }
        }
        Button(onClick = { crearAbierto = true; cabanaEnEdicion = null }, enabled = !estado.guardando) {
            Text("Agregar cabaña")
        }
        estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
        if (estado.cargando && estado.cabanas.isEmpty()) {
            CircularProgressIndicator(Modifier.padding(24.dp))
        } else if (estado.cabanas.isEmpty()) {
            Text("No hay cabañas registradas.", modifier = Modifier.padding(vertical = 16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(estado.cabanas, key = { it.id }) { cabana ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(cabana.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Estado: ${cabana.estado}")
                            Text(cabana.descripcion)
                            Text("Aforo ${cabana.aforoMaximo} · ${cabana.habitaciones} habitaciones · ${cabana.banos} baños")
                            Text("Tarifa por noche: ${formatearSoles(cabana.tarifaNoche)}")
                            Text("Imagen: ${cabana.imagenPrincipal}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { cabanaEnEdicion = cabana; crearAbierto = true },
                                    enabled = !estado.guardando
                                ) { Text("Editar") }
                                if (cabana.estado.equals("ACTIVA", ignoreCase = true)) {
                                    TextButton(
                                        onClick = { cabanaPorDesactivar = cabana },
                                        enabled = !estado.guardando
                                    ) { Text("Desactivar") }
                                } else {
                                    TextButton(
                                        onClick = { viewModel.cambiarEstado(cabana, activar = true) },
                                        enabled = !estado.guardando
                                    ) { Text("Activar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (crearAbierto) {
        FormularioCabanaDialog(
            cabana = cabanaEnEdicion,
            guardando = estado.guardando,
            error = estado.mensaje,
            onCerrar = { if (!estado.guardando) crearAbierto = false },
            onGuardar = { nombre, descripcion, aforo, habitaciones, banos, tarifa, imagen ->
                viewModel.guardar(cabanaEnEdicion?.id, nombre, descripcion, aforo, habitaciones, banos, tarifa, imagen)
            }
        )
    }

    cabanaPorDesactivar?.let { cabana ->
        AlertDialog(
            onDismissRequest = { cabanaPorDesactivar = null },
            title = { Text("Desactivar cabaña") },
            text = { Text("${cabana.nombre} dejará de aparecer en el catálogo. Su historial y sus reservas se conservarán.") },
            confirmButton = {
                TextButton(
                    enabled = !estado.guardando,
                    onClick = { viewModel.cambiarEstado(cabana, activar = false); cabanaPorDesactivar = null }
                ) { Text("Desactivar") }
            },
            dismissButton = { TextButton(onClick = { cabanaPorDesactivar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FormularioCabanaDialog(
    cabana: GestionCabana?,
    guardando: Boolean,
    error: String?,
    onCerrar: () -> Unit,
    onGuardar: (String, String, String, String, String, String, String) -> Unit
) {
    var nombre by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.nombre.orEmpty()) }
    var descripcion by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.descripcion.orEmpty()) }
    var aforo by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.aforoMaximo?.toString().orEmpty()) }
    var habitaciones by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.habitaciones?.toString().orEmpty()) }
    var banos by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.banos?.toString().orEmpty()) }
    var tarifa by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.tarifaNoche?.toString().orEmpty()) }
    var imagen by rememberSaveable(cabana?.id) { mutableStateOf(cabana?.imagenPrincipal.orEmpty()) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (cabana == null) "Nueva cabaña" else "Editar cabaña") },
        text = {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción") }, enabled = !guardando, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(aforo, { aforo = it.filter(Char::isDigit) }, label = { Text("Aforo máximo") }, singleLine = true, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(habitaciones, { habitaciones = it.filter(Char::isDigit) }, label = { Text("Habitaciones") }, singleLine = true, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(banos, { banos = it.filter(Char::isDigit) }, label = { Text("Baños") }, singleLine = true, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(tarifa, { tarifa = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Tarifa por noche (S/)") }, singleLine = true, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(imagen, { imagen = it }, label = { Text("URL o ruta de imagen") }, singleLine = true, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                Text("La imagen debe estar publicada en la web o usar una URL http/https.", style = MaterialTheme.typography.bodySmall)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !guardando, onClick = { onGuardar(nombre, descripcion, aforo, habitaciones, banos, tarifa, imagen) }) {
                if (guardando) CircularProgressIndicator() else Text("Guardar")
            }
        },
        dismissButton = { TextButton(enabled = !guardando, onClick = onCerrar) { Text("Cancelar") } }
    )
}
