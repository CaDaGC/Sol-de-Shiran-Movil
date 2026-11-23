package pe.edu.upeu.solshiran.presentation.reserva

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles
import pe.edu.upeu.solshiran.presentation.pago.PagoAdelantoSection

@Composable
fun ReservaScreen(
    idCabana: Long,
    autenticado: Boolean,
    onIniciarSesion: () -> Unit,
    onSesionInvalida: () -> Unit
) {
    val viewModel = koinViewModel<ReservaViewModel>(parameters = { parametersOf(idCabana) })
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.sesionInvalida) {
        if (estado.sesionInvalida) onSesionInvalida()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val reserva = estado.reserva
        if (reserva != null) {
            Text("Reserva temporal registrada", style = MaterialTheme.typography.titleLarge)
            Text("Código: ${reserva.codigo ?: reserva.id}")
            Text("Ingreso: ${estado.ingreso} a las 11:00")
            Text("Salida: ${estado.salida} a las 13:00")
            Text("Total: ${formatearSoles(reserva.total)}")
            Text("Plazo del adelanto: ${reserva.expiraEn.replace('T', ' ').take(16)}")
            PagoAdelantoSection(reserva, onSesionInvalida)
            return@Column
        }

        val cabana = estado.cabana
        if (cabana == null) {
            val error = estado.errorCabana
            if (error == null) {
                CircularProgressIndicator()
            } else {
                MensajeError(error)
                TextButton(onClick = viewModel::cargarCabana) { Text("Reintentar") }
            }
            return@Column
        }

        Text(cabana.nombre, style = MaterialTheme.typography.headlineSmall)
        Text("Selecciona el ingreso y luego la salida.")
        CalendarioReserva(
            estado = estado,
            hoy = viewModel.hoy,
            mesInicial = viewModel.mesInicial,
            onCambiarMes = viewModel::cambiarMes,
            onSeleccionar = viewModel::seleccionarFecha
        )
        val errorCalendario = estado.errorCalendario
        when {
            errorCalendario != null -> {
                MensajeError(errorCalendario)
                TextButton(onClick = { viewModel.cargarCalendario() }) { Text("Reintentar calendario") }
            }
            estado.ocupadas == null -> Text("Consultando fechas ocupadas…")
            else -> Text("Los días en rojo indican noches ocupadas. Consulta el rango para verificar los horarios.")
        }

        Text("Ingreso: ${estado.ingreso ?: "Por elegir"} · 11:00")
        Text("Salida: ${estado.salida ?: "Por elegir"} · 13:00")
        OutlinedTextField(
            value = estado.huespedes,
            onValueChange = viewModel::cambiarHuespedes,
            enabled = !estado.procesando,
            label = { Text("Huéspedes") },
            supportingText = { Text("Capacidad máxima: ${cabana.aforoMaximo}") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::cotizar,
            enabled = !estado.procesando && estado.salida != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Consultar disponibilidad") }

        if (estado.procesando) CircularProgressIndicator()
        estado.errorOperacion?.let { MensajeError(it) }
        estado.cotizacion?.let { cotizacion ->
            if (cotizacion.disponible) {
                Text("Fechas disponibles · ${cotizacion.noches} noche(s)")
                Text("Total: ${formatearSoles(cotizacion.total)}")
                Text("Adelanto: ${formatearSoles(cotizacion.adelanto)}")
                Text("Saldo: ${formatearSoles(cotizacion.saldo)}")
                Button(
                    onClick = if (autenticado) viewModel::reservar else onIniciarSesion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (autenticado) "Crear reserva temporal" else "Iniciar sesión para reservar")
                }
            } else {
                MensajeError(cotizacion.mensaje ?: "Las fechas seleccionadas no están disponibles.")
            }
        }
    }
}

private val meses = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)
private val diasSemana = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")

@Composable
private fun CalendarioReserva(
    estado: ReservaUiState,
    hoy: LocalDate,
    mesInicial: LocalDate,
    onCambiarMes: (Int) -> Unit,
    onSeleccionar: (LocalDate) -> Unit
) {
    val mes = estado.mes
    val siguiente = mes.plus(1, DateTimeUnit.MONTH)
    val inicio = mes.plus(1 - mes.dayOfWeek.isoDayNumber, DateTimeUnit.DAY)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${meses[mes.month.number - 1]} ${mes.year}", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onCambiarMes(-1) }, enabled = mes > mesInicial) { Text("Anterior") }
            TextButton(onClick = { onCambiarMes(1) }) { Text("Siguiente") }
        }
        Row {
            diasSemana.forEach { dia ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(dia) }
            }
        }
        var semana = inicio
        while (semana < siguiente) {
            Row {
                repeat(7) { posicion ->
                    val fecha = semana.plus(posicion, DateTimeUnit.DAY)
                    if (fecha < mes || fecha >= siguiente) {
                        Box(Modifier.weight(1f))
                    } else {
                        val ocupada = estado.ocupadas?.contains(fecha) == true
                        val extremo = fecha == estado.ingreso || fecha == estado.salida
                        val entreFechas = estado.ingreso?.let { fecha > it } == true &&
                            estado.salida?.let { fecha < it } == true
                        val colores = MaterialTheme.colorScheme
                        Surface(
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                .semantics {
                                    contentDescription = if (ocupada) "$fecha, noche ocupada" else "$fecha"
                                }
                                .clickable(
                                    enabled = fecha >= hoy && !estado.procesando,
                                    role = Role.Button
                                ) { onSeleccionar(fecha) },
                            color = when {
                                extremo -> colores.primaryContainer
                                ocupada -> colores.errorContainer
                                entreFechas -> colores.secondaryContainer
                                else -> colores.surface
                            },
                            contentColor = when {
                                extremo -> colores.onPrimaryContainer
                                ocupada -> colores.onErrorContainer
                                entreFechas -> colores.onSecondaryContainer
                                fecha < hoy -> colores.onSurface.copy(alpha = 0.38f)
                                else -> colores.onSurface
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(fecha.day.toString()) }
                        }
                    }
                }
            }
            semana = semana.plus(7, DateTimeUnit.DAY)
        }
    }
}

@Composable
private fun MensajeError(mensaje: String) {
    Text(mensaje, color = MaterialTheme.colorScheme.error)
}
