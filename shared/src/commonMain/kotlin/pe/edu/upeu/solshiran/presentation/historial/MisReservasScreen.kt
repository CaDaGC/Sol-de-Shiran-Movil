package pe.edu.upeu.solshiran.presentation.historial

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.domain.model.*
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun MisReservasScreen(sesion: Sesion?, onSesionInvalida: () -> Unit) {
    if (sesion?.rol != "CLIENTE") {
        Text("Inicia sesión como cliente para consultar tus reservas.", Modifier.padding(24.dp))
        return
    }
    val viewModel = koinViewModel<MisReservasViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var detalleId by rememberSaveable { mutableStateOf<Long?>(null) }
    LaunchedEffect(estado.sesionInvalida) { if (estado.sesionInvalida) onSesionInvalida() }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
        item {
            Text("Mis reservas", style = MaterialTheme.typography.titleLarge)
            Text("Consulta tus estancias activas y tu historial.")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GrupoMisReservas.entries.forEach { grupo ->
                    FilterChip(selected = estado.grupo == grupo, onClick = { detalleId = null; viewModel.cargar(grupo) }, label = { Text(grupo.etiqueta) })
                }
            }
            TextButton(onClick = { viewModel.cargar(estado.grupo, estado.resultado?.pagina ?: 0) }, enabled = !estado.cargando) { Text("Actualizar") }
        }
        when {
            estado.cargando -> item { CircularProgressIndicator() }
            estado.error != null -> item {
                Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { viewModel.cargar() }) { Text("Reintentar") }
            }
            else -> estado.resultado?.let { pagina ->
                if (pagina.reservas.isEmpty()) item { Text("No tienes reservas en esta sección.") }
                items(pagina.reservas, key = { it.id }) { reserva ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(reserva.cabana, style = MaterialTheme.typography.titleMedium)
                            Text("Reserva #${reserva.id} · "+etiquetaEstado(reserva.estado))
                            Text("Ingreso: ${reserva.ingreso} ${reserva.horaIngreso?.toString()?.take(5).orEmpty()}")
                            Text("Salida: ${reserva.salida} ${reserva.horaSalida?.toString()?.take(5).orEmpty()}")
                            Text("Total: "+formatearSoles(reserva.total))
                            TextButton(onClick = { detalleId = reserva.id; viewModel.verDetalle(reserva.id) }) { Text("Ver detalle") }
                        }
                    }
                }
                if (pagina.totalPaginas > 1 || pagina.pagina > 0) item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(enabled = pagina.pagina > 0, onClick = { viewModel.cargar(estado.grupo, pagina.pagina - 1) }) { Text("Anterior") }
                        Text("Página ${pagina.pagina + 1}", Modifier.padding(top = 12.dp))
                        TextButton(enabled = pagina.pagina + 1 < pagina.totalPaginas, onClick = { viewModel.cargar(estado.grupo, pagina.pagina + 1) }) { Text("Siguiente") }
                    }
                }
            }
        }
    }
    detalleId?.let { id ->
        AlertDialog(onDismissRequest = { detalleId = null; viewModel.cerrarDetalle() }, title = { Text("Detalle de mi reserva") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when {
                        estado.cargandoDetalle -> CircularProgressIndicator()
                        estado.errorDetalle != null -> {
                            Text(estado.errorDetalle.orEmpty(), color = MaterialTheme.colorScheme.error)
                            TextButton(onClick = { viewModel.verDetalle(id) }) { Text("Reintentar detalle") }
                        }
                        else -> estado.detalle?.let { reserva ->
                            Text(reserva.cabana, style = MaterialTheme.typography.titleMedium)
                            Text("Reserva #${reserva.id} · "+etiquetaEstado(reserva.estado))
                            Text("Ingreso: ${reserva.ingreso} ${reserva.horaIngreso?.toString()?.take(5).orEmpty()}")
                            Text("Salida: ${reserva.salida} ${reserva.horaSalida?.toString()?.take(5).orEmpty()}")
                            Text("Huéspedes: ${reserva.huespedes}")
                            Text("Tarifa por noche: "+formatearSoles(reserva.tarifa))
                            Text("Total: "+formatearSoles(reserva.total))
                            Text("Adelanto: "+formatearSoles(reserva.adelanto))
                            Text("Saldo pendiente: "+formatearSoles(reserva.saldo))
                            reserva.checkout?.let { salida ->
                                Text("Check-out: "+salida.fecha.replace('T', ' ').take(16))
                                salida.descripcion?.let { Text("Daños: $it") }
                                salida.monto?.let { Text("Importe de daños: "+formatearSoles(it)) }
                            }
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { detalleId = null; viewModel.cerrarDetalle() }) { Text("Cerrar") } })
    }
}

private fun etiquetaEstado(estado: EstadoReservaPanel): String = when (estado) {
    EstadoReservaPanel.PENDIENTE_PAGO -> "Pendiente de pago"
    EstadoReservaPanel.CONFIRMADA -> "Confirmada"
    EstadoReservaPanel.OCUPADA -> "En estadía"
    EstadoReservaPanel.FINALIZADA -> "Finalizada"
    EstadoReservaPanel.EXPIRADA -> "Expirada"
    EstadoReservaPanel.CANCELADA -> "Cancelada"
    EstadoReservaPanel.DESCONOCIDA -> "Estado sin identificar"
}
