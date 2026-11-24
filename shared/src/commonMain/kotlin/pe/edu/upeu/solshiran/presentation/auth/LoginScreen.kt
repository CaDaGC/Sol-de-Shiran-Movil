package pe.edu.upeu.solshiran.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onVolverCatalogo: () -> Unit,
    onRegistrarse: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    var mostrarContrasena by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sol de Shirán",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = estado.correo,
                onValueChange = viewModel::cambiarCorreo,
                label = {
                    Text("Correo electrónico")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.cargando,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = viewModel::cambiarContrasena,
                label = {
                    Text("Contraseña")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.cargando,
                singleLine = true,
                visualTransformation =
                    if (mostrarContrasena) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            mostrarContrasena = !mostrarContrasena
                        },
                        enabled = !estado.cargando
                    ) {
                        Text(
                            if (mostrarContrasena) {
                                "Ocultar"
                            } else {
                                "Mostrar"
                            }
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.ingresar()
                    }
                )
            )

            estado.mensaje?.let { mensaje ->
                Text(
                    text = mensaje,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = viewModel::ingresar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !estado.cargando &&
                        estado.correo.isNotBlank() &&
                        estado.contrasena.isNotBlank()
            ) {
                Text(
                    if (estado.cargando) {
                        "Ingresando..."
                    } else {
                        "Ingresar"
                    }
                )
            }

            TextButton(onClick = onRegistrarse, enabled = !estado.cargando) {
                Text("Crear cuenta")
            }

            TextButton(
                onClick = onVolverCatalogo,
                enabled = !estado.cargando
            ) {
                Text("Volver al catálogo")
            }
        }
    }
}
