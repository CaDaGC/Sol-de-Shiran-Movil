package pe.edu.upeu.solshiran.presentation.metricas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.data.remote.dto.IngresoDiaDto
import pe.edu.upeu.solshiran.presentation.catalogo.formatearSoles

@Composable
fun MetricasScreen() {
    val viewModel = koinViewModel<MetricasViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ingresos y reservas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = estado.desde,
                onValueChange = { viewModel.cambiarDesde(it.take(10)) },
                label = { Text("Desde") },
                supportingText = { Text("AAAA-MM-DD") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = estado.hasta,
                onValueChange = { viewModel.cambiarHasta(it.take(10)) },
                label = { Text("Hasta") },
                supportingText = { Text("AAAA-MM-DD") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Button(onClick = viewModel::cargar, enabled = !estado.cargando) { Text("Aplicar fechas") }

        if (estado.cargando) CircularProgressIndicator()
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Button(onClick = viewModel::cargar) { Text("Reintentar") }
        }

        estado.ingresos?.let { ingresos ->
            HorizontalDivider()
            Text("Ingresos aprobados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${formatearSoles(ingresos.totalIngresos)} en ${ingresos.numeroPagos} pagos")
            if (ingresos.ingresosPorDia.isEmpty()) {
                Text("No hay pagos aprobados en este periodo.")
            } else {
                GraficoIngresos(ingresos.ingresosPorDia)
            }
        }

        estado.reservas?.let { reservas ->
            HorizontalDivider()
            Text("Reservas por cabana", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${reservas.totalReservas} reservas en el periodo")
            if (reservas.ranking.isEmpty()) {
                Text("No hay reservas en este periodo.")
            } else {
                val maximo = reservas.ranking.maxOf { it.reservas }.coerceAtLeast(1)
                reservas.ranking.forEach { cabana ->
                    Text("Cabana ${cabana.idCabana}: ${cabana.reservas} reservas")
                    Box(Modifier.fillMaxWidth().height(12.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(
                            Modifier.fillMaxWidth(cabana.reservas.toFloat() / maximo.toFloat())
                                .height(12.dp).background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GraficoIngresos(dias: List<IngresoDiaDto>) {
    var seleccionado by remember(dias) { mutableStateOf(dias.last()) }
    val maximo = dias.maxOf { it.total }.coerceAtLeast(1.0)

    Text("Toca una barra para ver su importe.")
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        dias.forEach { dia ->
            val altura = (dia.total / maximo * 120).toFloat().coerceAtLeast(4f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.width(32.dp).height(altura.dp)
                        .background(if (dia == seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        .clickable { seleccionado = dia }
                )
                Text(dia.fecha.takeLast(5), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    Text("${seleccionado.fecha}: ${formatearSoles(seleccionado.total)} en ${seleccionado.operaciones} pagos")
}
