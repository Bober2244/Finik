package dev.bober.finik.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
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

    suspend fun onlineExtrasEnabled(): Boolean = dataStore.data.first()[ONLINE_EXTRAS] ?: false

    suspend fun saveOnlineExtrasEnabled(enabled: Boolean) {
        dataStore.edit { it[ONLINE_EXTRAS] = enabled }
    }

    suspend fun snapshotJson(): String? = dataStore.data.first()[SNAPSHOT]?.takeIf { it.isNotBlank() }

    suspend fun saveSnapshotJson(value: String) {
        dataStore.edit { it[SNAPSHOT] = value }
    }

    suspend fun clearPetCache() {
        dataStore.edit { it.remove(SNAPSHOT) }
    }

    suspend fun clearAll() {
        dataStore.edit {
            it.remove(SNAPSHOT)
            it.remove(TOKEN)
            it.remove(DEVICE_ID)
            it.remove(ONLINE_EXTRAS)
        }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("token")
        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val ONLINE_EXTRAS = booleanPreferencesKey("online_extras")
        private val SNAPSHOT = stringPreferencesKey("snapshot_json")
    }
}
