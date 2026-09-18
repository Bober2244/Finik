package dev.bober.finik.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StateEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FinikDatabase : RoomDatabase() {
    abstract fun stateDao(): StateDao
}
