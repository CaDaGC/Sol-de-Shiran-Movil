package pe.edu.upeu.solshiran.presentation.cuenta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.solshiran.domain.model.Sesion

@Composable
fun CuentaScreen(
    sesion: Sesion?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        if (sesion == null) {
            Text("No hay una sesión iniciada.")
        } else {
            Text("Nombre de usuario: ${sesion.nombreUsuario}")
            Text("Correo: ${sesion.correo}")
            Text("Rol: ${sesion.rol}")

            if (sesion.rol !in setOf("CLIENTE", "ADMIN")) {
                Text(
                    "El catálogo no está habilitado para tu rol."
                )
            }
        }
    }
}