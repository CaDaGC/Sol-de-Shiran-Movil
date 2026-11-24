package pe.edu.upeu.solshiran.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.solshiran.data.remote.AuthApiService
import pe.edu.upeu.solshiran.data.remote.CatalogoApiService
import pe.edu.upeu.solshiran.data.remote.HttpClientFactory
import pe.edu.upeu.solshiran.data.remote.GestionCabanasApiService
import pe.edu.upeu.solshiran.data.remote.PagoApiService
import pe.edu.upeu.solshiran.data.remote.PanelReservasApiService
import pe.edu.upeu.solshiran.data.remote.ReservaApiService
import pe.edu.upeu.solshiran.data.repository.AuthRepositoryImpl
import pe.edu.upeu.solshiran.data.repository.CabanaRepositoryImpl
import pe.edu.upeu.solshiran.data.repository.PagoRepositoryImpl
import pe.edu.upeu.solshiran.data.repository.PanelReservasRepositoryImpl
import pe.edu.upeu.solshiran.data.repository.GestionCabanasRepositoryImpl
import pe.edu.upeu.solshiran.data.repository.ReservaRepositoryImpl
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.CabanaRepository
import pe.edu.upeu.solshiran.domain.repository.PagoRepository
import pe.edu.upeu.solshiran.domain.repository.PanelReservasRepository
import pe.edu.upeu.solshiran.domain.repository.GestionCabanasRepository
import pe.edu.upeu.solshiran.domain.repository.ReservaRepository
import pe.edu.upeu.solshiran.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.ConsultarDisponibilidadUseCase
import pe.edu.upeu.solshiran.domain.usecase.ConsultarAdelantoUseCase
import pe.edu.upeu.solshiran.domain.usecase.CrearOrdenAdelantoUseCase
import pe.edu.upeu.solshiran.domain.usecase.ConsultarFechasOcupadasUseCase
import pe.edu.upeu.solshiran.domain.usecase.ConsultarPanelReservasUseCase
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleReservaPanelUseCase
import pe.edu.upeu.solshiran.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.ListarCabanasUseCase
import pe.edu.upeu.solshiran.domain.usecase.ListarCabanasPanelUseCase
import pe.edu.upeu.solshiran.domain.usecase.RecuperarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.RegistrarReservaUseCase
import pe.edu.upeu.solshiran.presentation.auth.LoginViewModel
import pe.edu.upeu.solshiran.presentation.catalogo.CatalogoViewModel
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleCabanaUseCase
import pe.edu.upeu.solshiran.presentation.detalle.DetalleCabanaViewModel
import pe.edu.upeu.solshiran.presentation.pago.PagoAdelantoViewModel
import pe.edu.upeu.solshiran.presentation.panel.PanelReservasViewModel
import pe.edu.upeu.solshiran.presentation.panel.DetalleReservaPanelViewModel
import pe.edu.upeu.solshiran.presentation.gestion.GestionCabanasViewModel
import pe.edu.upeu.solshiran.presentation.reserva.ReservaViewModel
import pe.edu.upeu.solshiran.data.remote.MisReservasApiService
import pe.edu.upeu.solshiran.data.remote.MetricasApiService
import pe.edu.upeu.solshiran.data.repository.MisReservasRepositoryImpl
import pe.edu.upeu.solshiran.domain.repository.MisReservasRepository
import pe.edu.upeu.solshiran.domain.usecase.ConsultarMisReservasUseCase
import pe.edu.upeu.solshiran.presentation.historial.MisReservasViewModel
import pe.edu.upeu.solshiran.presentation.metricas.MetricasViewModel

val dataModule = module {
    single { MetricasApiService(client = get(), configuracion = get()) }
    single { MisReservasApiService(client = get(), configuracion = get()) }
    single<MisReservasRepository> {
        MisReservasRepositoryImpl(api = get(), auth = get())
    }
    single { GestionCabanasApiService(client = get(), configuracion = get()) }
    single<GestionCabanasRepository> { GestionCabanasRepositoryImpl(api = get(), auth = get()) }
    single { PanelReservasApiService(client = get(), configuracion = get()) }
    single<PanelReservasRepository> { PanelReservasRepositoryImpl(api = get(), authRepository = get()) }
    single { PagoApiService(client = get(), configuracion = get()) }
    single<PagoRepository> { PagoRepositoryImpl(api = get(), authRepository = get()) }
    single { ReservaApiService(client = get(), configuracion = get()) }
    single<ReservaRepository> { ReservaRepositoryImpl(api = get(), authRepository = get()) }

    single {
        HttpClientFactory.crear(engine = get())
    }

    single {
        AuthApiService(
            client = get(),
            configuracion = get()
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            api = get(),
            almacen = get()
        )
    }

    single {
        CatalogoApiService(
            client = get(),
            configuracion = get()
        )
    }

    single<CabanaRepository> {
        CabanaRepositoryImpl(
            api = get(),
            authRepository = get(),
            configuracion = get()
        )
    }
}

val domainModule = module {
    factory { ConsultarMisReservasUseCase(repository = get()) }
    factory { ListarCabanasPanelUseCase(repository = get()) }
    factory { ConsultarPanelReservasUseCase(repository = get()) }
    factory { ObtenerDetalleReservaPanelUseCase(repository = get()) }
    factory { CrearOrdenAdelantoUseCase(repository = get()) }
    factory { ConsultarAdelantoUseCase(repository = get()) }
    factory { ConsultarFechasOcupadasUseCase(repository = get()) }
    factory { ConsultarDisponibilidadUseCase(repository = get()) }
    factory { RegistrarReservaUseCase(repository = get()) }

    factory {
        IniciarSesionUseCase(repository = get())
    }

    factory {
        RecuperarSesionUseCase(repository = get())
    }

    factory {
        CerrarSesionUseCase(repository = get())
    }

    factory {
        ListarCabanasUseCase(repository = get())
    }

    factory {
        ObtenerDetalleCabanaUseCase(repository = get())
    }
}

val presentationModule = module {
    viewModel { MetricasViewModel(api = get(), auth = get()) }
    viewModel { MisReservasViewModel(consultar = get()) }
    viewModel { GestionCabanasViewModel(repository = get()) }
    viewModel { PanelReservasViewModel(consultar = get(), listarCabanas = get()) }
    viewModel { (idReserva: Long) -> DetalleReservaPanelViewModel(idReserva = idReserva, obtenerDetalle = get()) }
    viewModel { (idReserva: Long) ->
        PagoAdelantoViewModel(
            idReserva = idReserva,
            crearOrden = get(),
            consultarAdelanto = get(),
            datosGuardados = get()
        )
    }

    viewModel { (idCabana: Long) ->
        ReservaViewModel(
            idCabana = idCabana,
            obtenerDetalle = get(),
            consultarFechas = get(),
            consultarDisponibilidad = get(),
            registrarReserva = get(),
            datosGuardados = get()
        )
    }

    viewModel {
        LoginViewModel(
            iniciarSesion = get(),
            recuperarSesion = get(),
            cerrarSesion = get()
        )
    }

    viewModel {
        CatalogoViewModel(
            listarCabanas = get()
        )
    }

    viewModel { (idCabana: Long) ->
        DetalleCabanaViewModel(
            idCabana = idCabana,
            obtenerDetalle = get()
        )
    }
}
