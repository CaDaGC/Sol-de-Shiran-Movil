package pe.edu.upeu.solshiran.di

import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module
import pe.edu.upeu.solshiran.data.local.AlmacenSesion
import pe.edu.upeu.solshiran.data.local.AlmacenSesionAndroid
import pe.edu.upeu.solshiran.data.remote.ConfiguracionApi

fun platformModuleAndroid(
    context: Context
) = module {
    single<HttpClientEngine> {
        OkHttp.create()
    }

    single<AlmacenSesion> {
        AlmacenSesionAndroid(
            context = context.applicationContext
        )
    }

    single {
        ConfiguracionApi(
            urlBase = "http://127.0.0.1:8061/api/v1/"
        )
    }
}