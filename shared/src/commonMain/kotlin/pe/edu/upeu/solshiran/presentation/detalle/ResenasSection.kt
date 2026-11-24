package pe.edu.upeu.solshiran.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ResenasSection(idCabana: Long) {
    val viewModel = koinViewModel<ResenasViewModel>(
        key = "resenas_$idCabana",
        parameters = { parametersOf(idCabana) }
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var puntuacion by remember(idCabana) { mutableIntStateOf(0) }
    var comentario by remember(idCabana) { mutableStateOf("") }

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje == "Resena publicada.") {
            puntuacion = 0
            comentario = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Opiniones de clientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        if (estado.opiniones.isEmpty() && !estado.cargando && estado.error == null) {
            Text("Esta cabana aun no tiene opiniones.")
        }
        estado.opiniones.forEach { opinion ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(opinion.autor ?: "Cliente", fontWeight = FontWeight.SemiBold)
                Text("${"★".repeat(opinion.puntuacion.coerceIn(0, 5))}  ${opinion.fecha?.substringBefore('T').orEmpty()}")
                Text(opinion.comentario)
            }
            HorizontalDivider()
        }

        if (estado.cargando) CircularProgressIndicator()
        estado.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = viewModel::reintentarCarga) {
                Text("Reintentar")
            }
        }
        if (estado.hayMas && !estado.cargando && estado.error == null) {
            TextButton(onClick = viewModel::cargarMas) { Text("Ver mas opiniones") }
        }

        HorizontalDivider()
        Text("Tu opinion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        when {
            estado.comprobando -> CircularProgressIndicator()
            !estado.esCliente -> Text("Inicia sesion como cliente para publicar una opinion.")
            estado.idReserva == null && estado.errorElegibilidad == null ->
                Text("Podras opinar cuando termine tu estancia. Si ya opinaste, no puedes repetirla para esa reserva.")
            estado.idReserva != null -> {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { valor ->
                        IconButton(
                            onClick = { puntuacion = valor },
                            enabled = !estado.enviando,
                            modifier = Modifier.semantics { contentDescription = "$valor estrellas" }
                        ) {
                            Text(if (valor <= puntuacion) "★" else "☆")
                        }
                    }
                }
                Text("Puntuacion: $puntuacion de 5")
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it.take(2000) },
                    label = { Text("Comentario") },
                    minLines = 3,
                    enabled = !estado.enviando,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { viewModel.publicar(puntuacion, comentario) },
                    enabled = !estado.enviando && puntuacion in 1..5 && comentario.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.enviando) "Publicando..." else "Publicar opinion")
                }
            }
        }
        estado.errorElegibilidad?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        if (estado.esCliente && estado.errorElegibilidad != null && estado.idReserva == null) {
            TextButton(onClick = viewModel::verificarElegibilidad) { Text("Reintentar") }
        }
    }
}
