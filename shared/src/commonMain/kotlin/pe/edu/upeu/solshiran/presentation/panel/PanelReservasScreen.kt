package pe.edu.upeu.solshiran.presentation.panel

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.domain.model.EstadoReservaPanel
import pe.edu.upeu.solshiran.domain.model.ReservaPanel
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun PanelReservasScreen(onSesionInvalida: () -> Unit) {
    val viewModel = koinViewModel<PanelReservasViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val lista = rememberLazyListState()
    var reservaAbierta by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(estado.sesionInvalida) {
        if (estado.sesionInvalida) onSesionInvalida()
    }
    LaunchedEffect(estado.filtro) {
        reservaAbierta = null
        lista.scrollToItem(0)
    }

    LazyColumn(
        state = lista,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            OutlinedTextField(
                value = estado.filtro.buscar,
                onValueChange = viewModel::buscar,
                label = { Text("Código, nombre o correo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            FiltroCabanas(estado, viewModel::seleccionarCabana, viewModel::actualizarCabanas)
            if (estado.hayFiltros) {
                TextButton(onClick = viewModel::limpiarFiltros) { Text("Limpiar filtros") }
            }
        }
        when {
            estado.cargando -> item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Cargando reservas…")
                }
            }
            estado.error != null -> item {
                TextoError(estado.error.orEmpty())
                TextButton(onClick = viewModel::reintentar) { Text("Reintentar") }
            }
            else -> estado.resultado?.let { pagina ->
                item {
                    Text(
                        if (pagina.totalReservas == 1L) "1 reserva" else "${pagina.totalReservas} reservas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (pagina.reservas.isEmpty()) {
                        Text(
                            when {
                                pagina.totalReservas > 0 -> "No hay reservas en esta página."
                                estado.hayFiltros -> "No se encontraron coincidencias."
                                else -> "No hay reservas para mostrar."
                            }
                        )
                    }
                }
                items(pagina.reservas, key = { it.id }) { reserva ->
                    TarjetaReserva(
                        reserva = reserva,
                        expandida = reservaAbierta == reserva.id,
                        onCambiar = {
                            reservaAbierta = if (reservaAbierta == reserva.id) null else reserva.id
                        }
                    )
                }
                if (pagina.totalPaginas > 1 || pagina.pagina > 0) {
                    item {
                        Paginacion(
                            pagina = pagina.pagina,
                            total = pagina.totalPaginas,
                            onCambiar = viewModel::cambiarPagina
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroCabanas(
    estado: PanelReservasUiState,
    onSeleccionar: (Long?) -> Unit,
    onReintentar: () -> Unit
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    val seleccion = estado.cabanas.firstOrNull { it.id == estado.filtro.idCabana }
    Box {
        OutlinedButton(
            enabled = !estado.cargandoCabanas && estado.errorCabanas == null && estado.cabanas.isNotEmpty(),
            onClick = { abierto = true }
        ) {
            Text(if (estado.cargandoCabanas) "Cargando cabañas…" else seleccion?.nombre ?: "Todas las cabañas")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(
                text = { Text("Todas las cabañas") },
                onClick = {
                    abierto = false
                    onSeleccionar(null)
                }
            )
            estado.cabanas.forEach { cabana ->
                DropdownMenuItem(
                    text = { Text(cabana.nombre) },
                    onClick = {
                        abierto = false
                        onSeleccionar(cabana.id)
                    }
                )
            }
        }
    }
    estado.errorCabanas?.let { mensaje ->
        TextoError(mensaje)
        TextButton(onClick = onReintentar) { Text("Reintentar cabañas") }
    }
}

@Composable
private fun TarjetaReserva(reserva: ReservaPanel, expandida: Boolean, onCambiar: () -> Unit) {
    Card(onClick = onCambiar, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(reserva.codigo, style = MaterialTheme.typography.labelLarge)
            Text(reserva.huesped, style = MaterialTheme.typography.titleMedium)
            Text(reserva.cabana)
            Text("Ingreso: ${fechaHora(reserva.ingreso, reserva.horaIngreso)}")
            Text("Salida: ${fechaHora(reserva.salida, reserva.horaSalida)}")
            Text("${reserva.estado.etiqueta()} · ${formatearSoles(reserva.total)}")
            if (expandida) {
                reserva.correo?.let { Text(it) }
                Text("Huéspedes: ${reserva.cantidadHuespedes}")
                Text("Saldo: ${formatearSoles(reserva.saldoPendiente)}")
            }
            Text(
                if (expandida) "Ocultar resumen" else "Ver resumen",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun Paginacion(pagina: Int, total: Int, onCambiar: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(enabled = pagina > 0, onClick = { onCambiar(-1) }) { Text("Anterior") }
        if (pagina < total) Text("Página ${pagina + 1} de $total")
        TextButton(enabled = pagina + 1 < total, onClick = { onCambiar(1) }) { Text("Siguiente") }
    }
}

@Composable
private fun TextoError(mensaje: String) {
    Text(mensaje, color = MaterialTheme.colorScheme.error)
}

private fun fechaHora(fecha: LocalDate, hora: LocalTime?): String {
    val dia = fecha.day.toString().padStart(2, '0')
    val mes = fecha.month.number.toString().padStart(2, '0')
    val fechaLocal = "$dia/$mes/${fecha.year}"
    return hora?.let { "$fechaLocal ${it.toString().take(5)}" } ?: fechaLocal
}

private fun EstadoReservaPanel.etiqueta(): String = when (this) {
    EstadoReservaPanel.PENDIENTE_PAGO -> "Pendiente de pago"
    EstadoReservaPanel.CONFIRMADA -> "Confirmada"
    EstadoReservaPanel.OCUPADA -> "En estadía"
    EstadoReservaPanel.FINALIZADA -> "Finalizada"
    EstadoReservaPanel.EXPIRADA -> "Expirada"
    EstadoReservaPanel.CANCELADA -> "Cancelada"
    EstadoReservaPanel.DESCONOCIDA -> "Estado sin identificar"
}
