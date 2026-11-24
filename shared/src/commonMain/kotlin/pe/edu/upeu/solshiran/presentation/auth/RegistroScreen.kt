package pe.edu.upeu.solshiran.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.data.remote.dto.PersonaRegistroDto
import pe.edu.upeu.solshiran.data.remote.dto.RegistroClienteDto

@Composable
fun RegistroScreen(onIniciarSesion: () -> Unit) {
    val viewModel = koinViewModel<RegistroViewModel>()
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var datos by remember {
        mutableStateOf(
            RegistroClienteDto("", "", "", PersonaRegistroDto("", "", "DNI", "", "", ""))
        )
    }

    LaunchedEffect(estado.completado) {
        if (estado.completado) datos = datos.copy(password = "")
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
        if (estado.completado) {
            Text("Tu cuenta se creo. Ya puedes iniciar sesion.")
            Button(onClick = onIniciarSesion) { Text("Iniciar sesion") }
        } else {
            Text("Datos de la cuenta", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = datos.nombreUsuario,
                onValueChange = { datos = datos.copy(nombreUsuario = it.take(80)) },
                label = { Text("Nombre de usuario") },
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = datos.email,
                onValueChange = { datos = datos.copy(email = it.take(150)) },
                label = { Text("Correo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = datos.password,
                onValueChange = { datos = datos.copy(password = it.take(72)) },
                label = { Text("Clave (8 a 72 caracteres)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Text("Datos personales", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = datos.persona.nombres,
                onValueChange = { datos = datos.copy(persona = datos.persona.copy(nombres = it.take(100))) },
                label = { Text("Nombres") },
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = datos.persona.apellidos,
                onValueChange = { datos = datos.copy(persona = datos.persona.copy(apellidos = it.take(100))) },
                label = { Text("Apellidos") },
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Text("Tipo de documento")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = datos.persona.tipoDocumento == "DNI",
                    onClick = { datos = datos.copy(persona = datos.persona.copy(tipoDocumento = "DNI")) },
                    enabled = !estado.cargando
                )
                Text("DNI")
                RadioButton(
                    selected = datos.persona.tipoDocumento == "CARNET_EXTRANJERIA",
                    onClick = { datos = datos.copy(persona = datos.persona.copy(tipoDocumento = "CARNET_EXTRANJERIA")) },
                    enabled = !estado.cargando
                )
                Text("Carnet")
            }
            OutlinedTextField(
                value = datos.persona.numeroDocumento,
                onValueChange = { datos = datos.copy(persona = datos.persona.copy(numeroDocumento = it.take(20))) },
                label = { Text("Numero de documento") },
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = datos.persona.telefono,
                onValueChange = { datos = datos.copy(persona = datos.persona.copy(telefono = it.take(20))) },
                label = { Text("Telefono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !estado.cargando, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = datos.persona.direccion,
                onValueChange = { datos = datos.copy(persona = datos.persona.copy(direccion = it.take(255))) },
                label = { Text("Direccion") },
                enabled = !estado.cargando, modifier = Modifier.fillMaxWidth()
            )

            estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { viewModel.registrar(datos) },
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (estado.cargando) "Creando cuenta..." else "Crear cuenta") }
            TextButton(onClick = onIniciarSesion, enabled = !estado.cargando) {
                Text("Ya tengo una cuenta")
            }
        }
    }
}
