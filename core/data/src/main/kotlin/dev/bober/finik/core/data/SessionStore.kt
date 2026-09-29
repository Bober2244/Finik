package dev.bober.finik.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.finikSessionStore by preferencesDataStore(name = "finik_session")

interface PlayerSession {
    suspend fun mode(): String
    suspend fun token(): String?
    suspend fun activate(mode: String, token: String)
    suspend fun clearToken()
    suspend fun saveToken(value: String)
    suspend fun deviceId(): String?
    suspend fun saveDeviceId(value: String)
    suspend fun serverAddress(): String?
    suspend fun saveServerAddress(address: String)
    suspend fun boundServer(): String?
    suspend fun selectServer(address: String, saveOverride: Boolean)
}

class SessionStore(context: Context) : PlayerSession {
    private val dataStore = context.applicationContext.finikSessionStore

    override suspend fun serverAddress(): String? = dataStore.data.first()[SERVER_ADDRESS]

    override suspend fun saveServerAddress(address: String) {
        dataStore.edit { it[SERVER_ADDRESS] = address }
    }

    override suspend fun boundServer(): String? = dataStore.data.first()[SESSION_SERVER]

    override suspend fun selectServer(address: String, saveOverride: Boolean) {
        dataStore.edit {
            it[SESSION_SERVER] = address
            if (saveOverride) it[SERVER_ADDRESS] = address
            it.remove(TOKEN)
            it.remove(DEMO_TOKEN)
            it.remove(SNAPSHOT)
        }
    }

    override suspend fun mode(): String = dataStore.data.first()[MODE] ?: "normal"

    override suspend fun token(): String? {
        val data = dataStore.data.first()
        return data[if (data[MODE] == "demo") DEMO_TOKEN else TOKEN]?.takeIf { it.isNotBlank() }
    }

    override suspend fun activate(mode: String, token: String) {
        dataStore.edit { data ->
            data[MODE] = mode
            data[if (mode == "demo") DEMO_TOKEN else TOKEN] = token
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { it.remove(if (it[MODE] == "demo") DEMO_TOKEN else TOKEN) }
    }


    override suspend fun saveToken(value: String) {
        dataStore.edit { it[if (it[MODE] == "demo") DEMO_TOKEN else TOKEN] = value }
    }

    override suspend fun deviceId(): String? = dataStore.data.first()[DEVICE_ID]?.takeIf { it.isNotBlank() }

    override suspend fun saveDeviceId(value: String) {
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
            it.remove(DEMO_TOKEN)
            it.remove(MODE)
            it.remove(DEVICE_ID)
            it.remove(ONLINE_EXTRAS)
        }
    }

    companion object {
        private val SERVER_ADDRESS = stringPreferencesKey("server_address")
        private val SESSION_SERVER = stringPreferencesKey("session_server")
        private val MODE = stringPreferencesKey("mode")
        private val DEMO_TOKEN = stringPreferencesKey("demo_token")
        private val TOKEN = stringPreferencesKey("token")
        private val DEVICE_ID = stringPreferencesKey("device_id")
        private val ONLINE_EXTRAS = booleanPreferencesKey("online_extras")
        private val SNAPSHOT = stringPreferencesKey("snapshot_json")
    }
}
