package pe.edu.upeu.solshiran.presentation.pago

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.solshiran.domain.model.EstadoAdelanto
import pe.edu.upeu.solshiran.domain.model.ReservaTemporal
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun PagoAdelantoSection(reserva: ReservaTemporal, onSesionInvalida: () -> Unit) {
    val viewModel = koinViewModel<PagoAdelantoViewModel>(
        key = "adelanto_${reserva.id}",
        parameters = { parametersOf(reserva.id) }
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val navegador = LocalUriHandler.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.consultarEstado()
    }
    LaunchedEffect(estado.sesionInvalida) {
        if (estado.sesionInvalida) onSesionInvalida()
    }

    Text("Adelanto: ${formatearSoles(estado.pago?.monto ?: reserva.adelanto)}")
    when (estado.pago?.estado) {
        EstadoAdelanto.APROBADA -> Text(
            "Adelanto aprobado. Tu reserva está confirmada.",
            color = MaterialTheme.colorScheme.primary
        )
        EstadoAdelanto.RECHAZADA -> Text("El pago no se aprobó.", color = MaterialTheme.colorScheme.error)
        EstadoAdelanto.PENDIENTE -> Text("Pago pendiente de confirmación.")
        null -> if (estado.idTransaccion != null) Text("Consulta el estado después de pagar.")
    }

    val url = estado.urlCheckout
    if (url != null) {
        Button(
            enabled = !estado.cargando && !estado.sesionInvalida,
            onClick = {
                try {
                    navegador.openUri(url)
                } catch (_: IllegalArgumentException) {
                    viewModel.avisarErrorNavegador()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Abrir Mercado Pago") }
        Text("Al terminar, vuelve a esta pantalla para verificar el resultado.")
    } else if (estado.puedePreparar) {
        Button(onClick = viewModel::prepararPago, modifier = Modifier.fillMaxWidth()) {
            Text(if (estado.idTransaccion == null) "Pagar adelanto" else "Preparar pago nuevamente")
        }
    }
    if (estado.idTransaccion != null && estado.pago?.estado != EstadoAdelanto.APROBADA) {
        TextButton(
            enabled = !estado.cargando && !estado.sesionInvalida,
            onClick = viewModel::consultarEstado
        ) { Text("Consultar estado del pago") }
    }
    if (estado.cargando) CircularProgressIndicator()
    estado.mensajeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}
