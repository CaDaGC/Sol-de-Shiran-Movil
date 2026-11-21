package pe.edu.upeu.solshiran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.presentation.auth.LoginScreen
import pe.edu.upeu.solshiran.presentation.auth.LoginViewModel
import pe.edu.upeu.solshiran.presentation.catalogo.CatalogoScreen

@Composable
fun App() {
    val loginViewModel = koinViewModel<LoginViewModel>()

    val estadoLogin by loginViewModel.estado
        .collectAsStateWithLifecycle()

    var mostrarLogin by rememberSaveable {
        mutableStateOf(false)
    }

    var habiaSesion by remember {
        mutableStateOf(false)
    }

    val sesion = estadoLogin.sesion

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        loginViewModel.validarVigencia()
    }

    LaunchedEffect(sesion) {
        if (sesion != null) {
            habiaSesion = true
            mostrarLogin = false
        } else if (habiaSesion) {
            habiaSesion = false
            mostrarLogin = true
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                estadoLogin.comprobandoSesion -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                sesion != null &&
                        sesion.rol !in setOf("CLIENTE", "ADMIN") -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            space = 16.dp,
                            alignment = Alignment.CenterVertically
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hola, ${sesion.nombreUsuario}",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text("Rol: ${sesion.rol}")

                        Text(
                            "El catálogo no está habilitado " +
                                    "para este rol."
                        )

                        Button(
                            onClick = loginViewModel::salir
                        ) {
                            Text("Cerrar sesión")
                        }
                    }
                }

                mostrarLogin -> {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onVolverCatalogo = {
                            mostrarLogin = false
                        }
                    )
                }

                else -> {
                    CatalogoScreen(
                        sesion = sesion,
                        onIniciarSesion = {
                            mostrarLogin = true
                        },
                        onCerrarSesion = loginViewModel::salir
                    )
                }
            }
        }
    }
}