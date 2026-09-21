package dev.bober.finik.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import kotlin.random.Random

class DeviceIdProvider(
    private val context: Context,
    private val session: SessionStore,
) {
    suspend fun get(): String {
        session.deviceId()?.let { return it }
        val created = build(androidId())
        session.saveDeviceId(created)
        return created
    }

    @SuppressLint("HardwareIds")
    private fun androidId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()

    companion object {
        internal fun build(androidId: String): String {
            val core = androidId.filter { it.isLetterOrDigit() }.ifBlank { "device" }
            val suffix = Random.nextBytes(4).joinToString("") { byte -> "%02x".format(byte) }
            return "android-$core-$suffix".take(128).padEnd(8, '0')
        }
    }
}
