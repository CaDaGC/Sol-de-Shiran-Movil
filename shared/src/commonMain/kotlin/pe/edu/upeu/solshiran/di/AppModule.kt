package pe.edu.upeu.solshiran.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.solshiran.data.remote.AuthApiService
import pe.edu.upeu.solshiran.data.remote.CatalogoApiService
import pe.edu.upeu.solshiran.data.remote.HttpClientFactory
import pe.edu.upeu.solshiran.data.repository.AuthRepositoryImpl
import pe.edu.upeu.solshiran.data.repository.CabanaRepositoryImpl
import pe.edu.upeu.solshiran.domain.repository.AuthRepository
import pe.edu.upeu.solshiran.domain.repository.CabanaRepository
import pe.edu.upeu.solshiran.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.solshiran.domain.usecase.ListarCabanasUseCase
import pe.edu.upeu.solshiran.domain.usecase.RecuperarSesionUseCase
import pe.edu.upeu.solshiran.presentation.auth.LoginViewModel
import pe.edu.upeu.solshiran.presentation.catalogo.CatalogoViewModel
import pe.edu.upeu.solshiran.domain.usecase.ObtenerDetalleCabanaUseCase
import pe.edu.upeu.solshiran.presentation.detalle.DetalleCabanaViewModel

val dataModule = module {
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