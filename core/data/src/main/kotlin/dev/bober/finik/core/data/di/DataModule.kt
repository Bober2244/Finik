package dev.bober.finik.core.data.di

import androidx.room.Room
import dev.bober.finik.core.data.DeviceIdProvider
import dev.bober.finik.core.data.FinikRepository
import dev.bober.finik.core.data.FinikViewModel
import dev.bober.finik.core.data.SessionStore
import dev.bober.finik.core.data.RoomSnapshotStore
import dev.bober.finik.core.database.FinikDatabase
import dev.bober.finik.core.network.NetworkConfig
import dev.bober.finik.core.network.createFinikApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), FinikDatabase::class.java, "finik.db")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    singleOf(::SessionStore)
    single { DeviceIdProvider(get<SessionStore>()) }
    single { FinikRepository(cacheStore = RoomSnapshotStore(get()), apiFactory = { address, tokenProvider -> createFinikApi(address, tokenProvider) }, session = get<SessionStore>(), deviceIds = get(), networkConfig = get()) }
    viewModelOf(::FinikViewModel)
}
