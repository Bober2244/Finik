package dev.bober.finik.core.data

import java.util.UUID

class DeviceIdProvider(
    private val session: SessionStore,
) {
    suspend fun get(): String {
        session.deviceId()?.takeIf { it.startsWith("finik-") }?.let { return it }
        val created = build()
        session.saveDeviceId(created)
        return created
    }

    companion object {
        internal fun build(): String = "finik-${UUID.randomUUID()}"
    }
}
