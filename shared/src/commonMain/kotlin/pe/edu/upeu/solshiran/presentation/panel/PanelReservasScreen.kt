package pe.edu.upeu.solshiran.presentation.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
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
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.solshiran.domain.model.EstadoReservaPanel
import pe.edu.upeu.solshiran.domain.model.MetodoPagoRestante
import pe.edu.upeu.solshiran.domain.model.ReservaPanel
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun PanelReservasScreen(onSesionInvalida: () -> Unit) {
    val viewModel = koinViewModel<PanelReservasViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val lista = rememberLazyListState()
    var reservaAbierta by rememberSaveable { mutableStateOf<Long?>(null) }
    var detalleReservaId by rememberSaveable { mutableStateOf<Long?>(null) }

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
                        },
                        onDetalle = { detalleReservaId = reserva.id }
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

    detalleReservaId?.let { idReserva ->
        DetalleReservaPanelDialog(
            idReserva = idReserva,
            onCerrar = { detalleReservaId = null; viewModel.reintentar() },
            onSesionInvalida = onSesionInvalida
        )
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
private fun TarjetaReserva(
    reserva: ReservaPanel,
    expandida: Boolean,
    onCambiar: () -> Unit,
    onDetalle: () -> Unit
) {
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
                TextButton(onClick = onDetalle) { Text("Ver detalle completo") }
            }
            Text(
                if (expandida) "Ocultar resumen" else "Ver resumen",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DetalleReservaPanelDialog(
    idReserva: Long,
    onCerrar: () -> Unit,
    onSesionInvalida: () -> Unit
) {
    val viewModel = koinViewModel<DetalleReservaPanelViewModel>(
        key = "detalle_reserva_$idReserva",
        parameters = { parametersOf(idReserva) }
    )
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var metodoPago by rememberSaveable(idReserva) { mutableStateOf(MetodoPagoRestante.EFECTIVO.name) }
    var numeroOperacion by rememberSaveable(idReserva) { mutableStateOf("") }
    LaunchedEffect(estado.sesionInvalida) {
        if (estado.sesionInvalida) onSesionInvalida()
    }
    var salidaAbierta by rememberSaveable(idReserva) { mutableStateOf(false) }
    LaunchedEffect(estado.detalle?.estado) {
        if (estado.detalle?.estado == EstadoReservaPanel.FINALIZADA) salidaAbierta = false
    }
    if (salidaAbierta) SalidaReservaDialog(idReserva, estado.registrandoSalida, estado.errorSalida,
        onCerrar = { salidaAbierta = false }, onGuardar = viewModel::registrarSalida)
    val detalle = estado.detalle
    val pagoRestante = estado.pagoRestante
    if (!salidaAbierta) AlertDialog(
        onDismissRequest = { if (!estado.registrandoSalida) onCerrar() },
        title = { Text("Detalle de reserva") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    estado.cargando -> {
                        CircularProgressIndicator()
                        Text("Cargando detalle…")
                    }
                    estado.error != null -> {
                        Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::cargar) { Text("Reintentar") }
                    }
                    detalle != null -> {
                        Text(detalle.cabana ?: "Cabaña #${detalle.idCabana}", style = MaterialTheme.typography.titleMedium)
                        Text("Reserva #${detalle.id}")
                        Text("Estado: ${detalle.estado.etiqueta()}")
                        Text("Cliente: ${detalle.huesped}")
                        detalle.correo?.let { Text("Correo: $it") }
                        Text("Ingreso: ${fechaHora(detalle.ingreso, detalle.horaIngreso)}")
                        Text("Salida: ${fechaHora(detalle.salida, detalle.horaSalida)}")
                        Text("Huéspedes: ${detalle.cantidadHuespedes}")
                        Text("Tarifa por noche: ${formatearSoles(detalle.tarifaNoche)}")
                        Text("Total: ${formatearSoles(detalle.total)}")
                        Text("Adelanto: ${formatearSoles(detalle.adelanto)}")
                        Text("Saldo pendiente: ${formatearSoles(detalle.saldoPendiente)}")
                        detalle.fechaCreacion?.let { Text("Creada: ${it.replace('T', ' ').take(16)}") }
                        detalle.expiraEn?.let { Text("Vence: ${it.replace('T', ' ').take(16)}") }
                        detalle.checkout?.let { salida ->
                            Text("Check-out: "+salida.fecha.replace('T', ' ').take(16))
                            salida.descripcion?.let { Text("Daños: $it") }
                            salida.monto?.let { Text("Cobro por daños: "+formatearSoles(it)) }
                            salida.medioPago?.let { Text("Medio de pago: $it") }
                        }
                        if (detalle.estado == EstadoReservaPanel.OCUPADA) {
                            OutlinedButton(onClick = { salidaAbierta = true }, enabled = !estado.registrandoSalida) { Text("Registrar check-out") }
                        }
                        if (pagoRestante?.completado == true) {
                            Text("Pago restante registrado: ${formatearSoles(pagoRestante.monto)}")
                            Text("Método: ${pagoRestante.metodoPago.etiqueta}")
                            pagoRestante.numeroOperacion?.let { Text("Operación: $it") }
                        } else if (detalle.estado == EstadoReservaPanel.CONFIRMADA && detalle.saldoPendiente > 0) {
                            Text("Registrar pago restante y check-in", style = MaterialTheme.typography.titleSmall)
                            if (estado.consultandoPago) {
                                CircularProgressIndicator()
                                Text("Verificando si ya existe un pago…")
                            } else if (estado.errorPago != null) {
                                Text(estado.errorPago.orEmpty(), color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = viewModel::cargar) { Text("Verificar nuevamente") }
                            } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                MetodoPagoRestante.entries.forEach { metodo ->
                                    OutlinedButton(
                                        enabled = !estado.registrandoPago,
                                        onClick = { metodoPago = metodo.name },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(if (metodoPago == metodo.name) "✓ ${metodo.etiqueta}" else metodo.etiqueta) }
                                }
                            }
                            val metodo = MetodoPagoRestante.entries.first { it.name == metodoPago }
                            if (metodo != MetodoPagoRestante.EFECTIVO) {
                                OutlinedTextField(
                                    value = numeroOperacion,
                                    onValueChange = { if (it.length <= 100) numeroOperacion = it },
                                    label = { Text("Número de operación") },
                                    singleLine = true,
                                    enabled = !estado.registrandoPago,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            estado.errorPago?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            TextButton(
                                enabled = !estado.registrandoPago &&
                                    (metodo == MetodoPagoRestante.EFECTIVO || numeroOperacion.isNotBlank()),
                                onClick = { viewModel.registrarPago(metodo, numeroOperacion) }
                            ) {
                                if (estado.registrandoPago) CircularProgressIndicator()
                                else Text("Cobrar saldo y registrar check-in")
                            }
                            }
                        } else {
                            estado.errorPago?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar, enabled = !estado.registrandoSalida) { Text("Cerrar") } }
    )
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
