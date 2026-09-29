package dev.bober.finik.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "finik_state")
data class StateEntity(
    @PrimaryKey val id: Int = 1,
    val json: String,
)

@Dao
interface StateDao {
    @Query("SELECT * FROM finik_state WHERE id = :id")
    suspend fun get(id: Int = 1): StateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StateEntity)

    @Query("DELETE FROM finik_state WHERE id = :id")
    suspend fun clearProfile(id: Int)

    @Query("DELETE FROM finik_state")
    suspend fun clear()
}
