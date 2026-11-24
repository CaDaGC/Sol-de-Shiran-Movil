package pe.edu.upeu.solshiran.presentation.panel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.solshiran.domain.model.*

@Composable
fun SalidaReservaDialog(idReserva: Long, guardando: Boolean, error: String?, onCerrar: () -> Unit, onGuardar: (DanosReserva?) -> Unit) {
    var hayDanos by rememberSaveable(idReserva) { mutableStateOf(false) }
    var descripcion by rememberSaveable(idReserva) { mutableStateOf("") }
    var importe by rememberSaveable(idReserva) { mutableStateOf("") }
    var medio by rememberSaveable(idReserva) { mutableStateOf(MedioPagoDanos.EFECTIVO.name) }
    var errorFormulario by rememberSaveable(idReserva) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        title = { Text("Registrar check-out") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Reserva #$idReserva. Al confirmar se finaliza la estancia y se libera la cabaña.")
                Row { Checkbox(hayDanos, { hayDanos = it; errorFormulario = null }, enabled = !guardando); Text("Registrar cobro por daños", Modifier.padding(top = 12.dp)) }
                if (hayDanos) {
                    OutlinedTextField(descripcion, { descripcion = it }, label = { Text("Descripción de los daños") }, enabled = !guardando, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(importe, { importe = it }, label = { Text("Importe recibido (S/)") }, enabled = !guardando, singleLine = true, modifier = Modifier.fillMaxWidth())
                    MedioPagoDanos.entries.forEach { opcion ->
                        OutlinedButton(onClick = { medio = opcion.name }, enabled = !guardando) { Text(if (medio == opcion.name) "✓ "+opcion.etiqueta else opcion.etiqueta) }
                    }
                    Text("Registra este importe solo después de recibir el pago.")
                }
                (errorFormulario ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(enabled = !guardando, onClick = {
                try {
                    val danos = prepararDanos(hayDanos, descripcion, importe, MedioPagoDanos.valueOf(medio))
                    errorFormulario = null
                    onGuardar(danos)
                } catch (e: IllegalArgumentException) { errorFormulario = e.message }
            }) { if (guardando) CircularProgressIndicator() else Text("Confirmar salida") }
        },
        dismissButton = { TextButton(onClick = onCerrar, enabled = !guardando) { Text("Cancelar") } }
    )
}
