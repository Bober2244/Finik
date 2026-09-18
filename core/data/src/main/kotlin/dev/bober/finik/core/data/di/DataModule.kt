package dev.bober.finik.core.data.di

import androidx.room.Room
import dev.bober.finik.core.data.FinikRepository
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.database.FinikDatabase
import dev.bober.finik.core.network.ContentApi
import dev.bober.finik.core.network.FinikHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), FinikDatabase::class.java, "finik.db")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { FinikHttp.client() }
    single { ContentApi(get()) }
    single { FinikRepository(db = get(), api = get()) }
    viewModelOf(::FinikViewModel)
}
