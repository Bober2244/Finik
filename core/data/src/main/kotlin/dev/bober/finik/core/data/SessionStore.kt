package dev.bober.finik.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.finikSessionStore by preferencesDataStore(name = "finik_session")

class SessionStore(context: Context) {
    private val dataStore = context.applicationContext.finikSessionStore

    suspend fun token(): String? = dataStore.data.first()[TOKEN]?.takeIf { it.isNotBlank() }

    suspend fun saveToken(value: String) {
        dataStore.edit { it[TOKEN] = value }
    }

    suspend fun deviceId(): String? = dataStore.data.first()[DEVICE_ID]?.takeIf { it.isNotBlank() }

    suspend fun saveDeviceId(value: String) {
        dataStore.edit { it[DEVICE_ID] = value }
    }

    suspend fun snapshotJson(): String? = dataStore.data.first()[SNAPSHOT]?.takeIf { it.isNotBlank() }

    suspend fun saveSnapshotJson(value: String) {
        dataStore.edit { it[SNAPSHOT] = value }
    }

    suspend fun clearPetCache() {
        dataStore.edit { it.remove(SNAPSHOT) }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("token")
        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val SNAPSHOT = stringPreferencesKey("snapshot_json")
    }
}
