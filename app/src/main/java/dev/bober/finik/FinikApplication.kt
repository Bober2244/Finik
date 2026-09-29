package dev.bober.finik

import android.app.Application
import dev.bober.finik.core.data.di.dataModule
import dev.bober.finik.core.network.NetworkConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class FinikApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FinikApplication)
            modules(
                module {
                    single {
                        NetworkConfig(
                            BuildConfig.API_BASE_URL,
                            allowInsecure = BuildConfig.DEBUG,
                            canChangeServer = BuildConfig.DEBUG,
                        )
                    }
                },
                dataModule,
            )
        }
    }
}
