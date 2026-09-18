package dev.bober.finik

import android.app.Application
import dev.bober.finik.core.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FinikApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FinikApplication)
            modules(dataModule)
        }
    }
}
