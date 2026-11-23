package pe.edu.upeu.solshiran

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.toRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.solshiran.domain.model.Sesion
import pe.edu.upeu.solshiran.domain.model.puedeConsultarPanelReservas
import pe.edu.upeu.solshiran.navigation.Catalogo
import pe.edu.upeu.solshiran.navigation.Cuenta
import pe.edu.upeu.solshiran.navigation.DetalleCabana
import pe.edu.upeu.solshiran.navigation.Login
import pe.edu.upeu.solshiran.navigation.PanelReservas
import pe.edu.upeu.solshiran.navigation.GestionCabanas
import pe.edu.upeu.solshiran.navigation.ReservarCabana
import pe.edu.upeu.solshiran.presentation.auth.LoginScreen
import pe.edu.upeu.solshiran.presentation.auth.LoginViewModel
import pe.edu.upeu.solshiran.presentation.catalogo.CatalogoScreen
import pe.edu.upeu.solshiran.presentation.components.IconoMenu
import pe.edu.upeu.solshiran.presentation.components.IconoVolver
import pe.edu.upeu.solshiran.presentation.components.MenuLateral
import pe.edu.upeu.solshiran.presentation.cuenta.CuentaScreen
import pe.edu.upeu.solshiran.presentation.detalle.DetalleCabanaScreen
import pe.edu.upeu.solshiran.presentation.panel.PanelReservasScreen
import pe.edu.upeu.solshiran.presentation.gestion.GestionCabanasScreen
import pe.edu.upeu.solshiran.presentation.reserva.ReservaScreen

@Composable
fun App() {
    val loginViewModel = koinViewModel<LoginViewModel>()
    val estado by loginViewModel.estado.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        loginViewModel.validarVigencia()
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            if (estado.comprobandoSesion) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val sesion = estado.sesion

                val inicio: Any = when {
                    sesion?.rol == "TRABAJADOR" -> PanelReservas

                    sesion != null &&
                            sesion.rol !in setOf("CLIENTE", "ADMIN") -> Cuenta

                    sesion != null -> Catalogo

                    estado.mensaje != null -> Login

                    else -> Catalogo
                }

                key(sesion?.idUsuario, sesion?.rol) {
                    NavegacionPrincipal(
                        sesion = sesion,
                        inicio = inicio,
                        loginViewModel = loginViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavegacionPrincipal(
    sesion: Sesion?,
    inicio: Any,
    loginViewModel: LoginViewModel
) {
    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val destino = entrada?.destination

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    val puedeVerCatalogo = sesion == null ||
            sesion.rol in setOf("CLIENTE", "ADMIN")

    val esCatalogo = destino?.hasRoute<Catalogo>() == true
    val esDetalle = destino?.hasRoute<DetalleCabana>() == true
    val esCuenta = destino?.hasRoute<Cuenta>() == true
    val esPanel = destino?.hasRoute<PanelReservas>() == true
    val esGestionCabanas = destino?.hasRoute<GestionCabanas>() == true

    val titulo = when {
        esCatalogo -> "Catálogo"
        esDetalle -> "Detalle de cabaña"
        destino?.hasRoute<ReservarCabana>() == true -> "Reservar cabaña"
        esCuenta -> "Mi cuenta"
        esPanel -> "Panel de reservas"
        esGestionCabanas -> "Gestión de cabañas"
        else -> "Iniciar sesión"
    }

    val puedeVolver = navController.previousBackStackEntry != null

    fun volverAlCatalogo() {
        if (!puedeVerCatalogo) {
            return
        }

        if (!navController.popBackStack<Catalogo>(inclusive = false)) {
            navController.navigate(Catalogo) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }

                launchSingleTop = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerState = drawerState
            ) {
                MenuLateral(
                    sesion = sesion,
                    catalogoSeleccionado = esCatalogo || esDetalle,
                    cuentaSeleccionada = esCuenta,
                    panelSeleccionado = esPanel,
                    gestionCabanasSeleccionado = esGestionCabanas,
                    onCatalogo = {
                        scope.launch {
                            drawerState.close()
                            volverAlCatalogo()
                        }
                    },
                    onPanelReservas = {
                        scope.launch {
                            drawerState.close()
                            if (!navController.popBackStack<PanelReservas>(inclusive = false)) {
                                navController.navigate(PanelReservas) { launchSingleTop = true }
                            }
                        }
                    },
                    onGestionCabanas = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(GestionCabanas) { launchSingleTop = true }
                        }
                    },
                    onCuenta = {
                        scope.launch {
                            drawerState.close()

                            navController.navigate(Cuenta) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onIniciarSesion = {
                        scope.launch {
                            drawerState.close()

                            navController.navigate(Login) {
                                launchSingleTop = true
                            }
                        }
                    },
                    onCerrarSesion = {
                        scope.launch {
                            drawerState.close()
                            loginViewModel.salir()
                        }
                    },
                    onCerrarMenu = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = titulo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        Row {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                },
                                modifier = Modifier.semantics {
                                    contentDescription =
                                        "Abrir menú de navegación"
                                }
                            ) {
                                IconoMenu()
                            }

                            if (puedeVolver) {
                                IconButton(
                                    onClick = {
                                        navController.popBackStack()
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Volver"
                                    }
                                ) {
                                    IconoVolver()
                                }
                            }
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = inicio,
                modifier = Modifier.padding(padding)
            ) {
                composable<Catalogo> {
                    if (puedeVerCatalogo) {
                        CatalogoScreen(
                            onVerDetalle = { idCabana ->
                                navController.navigate(
                                    DetalleCabana(idCabana)
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        CuentaScreen(sesion)
                    }
                }

                composable<DetalleCabana> { entry ->
                    if (puedeVerCatalogo) {
                        val ruta = entry.toRoute<DetalleCabana>()

                        DetalleCabanaScreen(
                            idCabana = ruta.idCabana,
                            onVolverCatalogo = {
                                volverAlCatalogo()
                            },
                            onReservar = {
                                navController.navigate(ReservarCabana(ruta.idCabana)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        CuentaScreen(sesion)
                    }
                }

                composable<ReservarCabana> { entry ->
                    if (puedeVerCatalogo) {
                        ReservaScreen(
                            idCabana = entry.toRoute<ReservarCabana>().idCabana,
                            autenticado = sesion != null,
                            onIniciarSesion = {
                                navController.navigate(Login) { launchSingleTop = true }
                            },
                            onSesionInvalida = loginViewModel::invalidarSesion
                        )
                    } else {
                        CuentaScreen(sesion)
                    }
                }

                composable<Login> {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onVolverCatalogo = {
                            volverAlCatalogo()
                        }
                    )
                }

                composable<PanelReservas> {
                    if (sesion.puedeConsultarPanelReservas()) {
                        PanelReservasScreen(onSesionInvalida = loginViewModel::invalidarSesion)
                    } else {
                        CuentaScreen(sesion)
                    }
                }

                composable<GestionCabanas> {
                    GestionCabanasScreen(
                        sesion = sesion,
                        onSesionInvalida = loginViewModel::invalidarSesion
                    )
                }

                composable<Cuenta> {
                    CuentaScreen(sesion)
                }
            }
        }
    }
}
