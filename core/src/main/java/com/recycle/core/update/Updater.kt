package com.recycle.core.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

/** Shape of the remote update manifest (hosted as raw JSON, e.g. on GitHub). */
@Serializable
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String = "",
    val apkUrl: String,
    val notes: String = "",
)

/** What the UI needs once an update is confirmed available. */
data class UpdateInfo(
    val versionName: String,
    val notes: String,
    val apkUrl: String,
)

/**
 * Self-hosted in-app updater for sideloaded builds (no Play Store / OTA).
 * On launch the app fetches a small manifest; if it advertises a higher
 * versionCode than the running build, the user is offered the new APK.
 */
object Updater {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Returns [UpdateInfo] when the remote manifest advertises a newer
     * versionCode, else null. Never throws — a failed/absent check is silent
     * so a flaky network never nags or blocks the user.
     */
    suspend fun check(manifestUrl: String, currentVersionCode: Int): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder().url(manifestUrl)
                    .header("Cache-Control", "no-cache").get().build()
                http.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val m = json.decodeFromString(
                        UpdateManifest.serializer(), resp.body?.string().orEmpty()
                    )
                    if (m.versionCode > currentVersionCode && m.apkUrl.isNotBlank())
                        UpdateInfo(m.versionName, m.notes, m.apkUrl)
                    else null
                }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Streams the APK to app-private external storage (no runtime storage
     * permission needed; reachable by the install FileProvider). Returns the
     * downloaded file, or null on failure.
     */
    suspend fun downloadApk(context: Context, apkUrl: String): File? =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.getExternalFilesDir(null), "apk").apply { mkdirs() }
                val out = File(dir, "update.apk")
                val req = Request.Builder().url(apkUrl).get().build()
                http.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val body = resp.body ?: return@withContext null
                    out.outputStream().use { fo -> body.byteStream().copyTo(fo) }
                }
                out
            } catch (e: Exception) {
                null
            }
        }
}
