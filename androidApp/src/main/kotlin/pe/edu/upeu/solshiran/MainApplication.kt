package pe.edu.upeu.solshiran

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pe.edu.upeu.solshiran.di.dataModule
import pe.edu.upeu.solshiran.di.domainModule
import pe.edu.upeu.solshiran.di.platformModuleAndroid
import pe.edu.upeu.solshiran.di.presentationModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)

            modules(
                platformModuleAndroid(this@MainApplication),
                dataModule,
                domainModule,
                presentationModule
            )
        }
    }
}