package pt.condeco.peso

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import pt.condeco.peso.di.appModule

class PesoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PesoApp)
            modules(appModule)
        }
    }
}
