package dev.bober.finik.core.data

import dev.bober.finik.core.database.FinikDatabase
import dev.bober.finik.core.database.StateEntity

interface SnapshotStore {
    suspend fun read(profileId: Int): String?
    suspend fun write(profileId: Int, json: String)
    suspend fun clear(profileId: Int)
}

class RoomSnapshotStore(private val database: FinikDatabase) : SnapshotStore {
    override suspend fun read(profileId: Int) = database.stateDao().get(profileId)?.json
    override suspend fun write(profileId: Int, json: String) = database.stateDao().upsert(StateEntity(profileId, json))
    override suspend fun clear(profileId: Int) = database.stateDao().clearProfile(profileId)
}
