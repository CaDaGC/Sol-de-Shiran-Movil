package pe.edu.upeu.solshiran.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.domain.model.puedeConsultarPanelReservas

@Composable
fun MenuLateral(
    sesion: Sesion?,
    catalogoSeleccionado: Boolean,
    cuentaSeleccionada: Boolean,
    panelSeleccionado: Boolean,
    gestionCabanasSeleccionado: Boolean,
    misReservasSeleccionado: Boolean,
    metricasSeleccionadas: Boolean,
    onCatalogo: () -> Unit,
    onPanelReservas: () -> Unit,
    onGestionCabanas: () -> Unit,
    onMisReservas: () -> Unit,
    onMetricas: () -> Unit,
    onCuenta: () -> Unit,
    onIniciarSesion: () -> Unit,
    onCerrarSesion: () -> Unit,
    onCerrarMenu: () -> Unit
) {
    val puedeVerCatalogo = sesion == null ||
            sesion.rol in setOf("CLIENTE", "ADMIN")

    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sol de Shirán",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onCerrarMenu) {
                Text("Cerrar")
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Navegación",
                style = MaterialTheme.typography.labelLarge
            )

            if (puedeVerCatalogo) {
                NavigationDrawerItem(
                    label = {
                        Text("Catálogo")
                    },
                    selected = catalogoSeleccionado,
                    onClick = onCatalogo
                )
            }

            if (sesion?.rol == "CLIENTE") {
                NavigationDrawerItem(
                    label = { Text("Mis reservas") },
                    selected = misReservasSeleccionado,
                    onClick = onMisReservas
                )
            }

            if (sesion?.rol == "ADMIN") {
                NavigationDrawerItem(
                    label = { Text("Gestión de cabañas") },
                    selected = gestionCabanasSeleccionado,
                    onClick = onGestionCabanas
                )
            }

            if (sesion?.rol in setOf("ADMIN", "DUENO")) {
                NavigationDrawerItem(
                    label = { Text("Metricas") },
                    selected = metricasSeleccionadas,
                    onClick = onMetricas
                )
            }

            if (sesion.puedeConsultarPanelReservas()) {
                NavigationDrawerItem(
                    label = { Text("Panel de reservas") },
                    selected = panelSeleccionado,
                    onClick = onPanelReservas
                )
            }

            if (sesion != null) {
                NavigationDrawerItem(
                    label = {
                        Text("Mi cuenta")
                    },
                    selected = cuentaSeleccionada,
                    onClick = onCuenta
                )
            }
        }

        HorizontalDivider()

        if (sesion == null) {
            Text(
                text = "Estás navegando como invitado.",
                style = MaterialTheme.typography.bodyMedium
            )

            TextButton(onClick = onIniciarSesion) {
                Text("Iniciar sesión")
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sesion.nombreUsuario
                                .firstOrNull()
                                ?.uppercase()
                                ?: "U",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = sesion.rol,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        text = sesion.nombreUsuario,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = sesion.correo,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            TextButton(onClick = onCerrarSesion) {
                Text("Cerrar sesión")
            }
        }
    }
}
