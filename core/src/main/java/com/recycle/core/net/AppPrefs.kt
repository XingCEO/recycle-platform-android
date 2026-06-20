package com.recycle.core.net

import android.content.Context
import com.recycle.core.BuildConfig

/**
 * Persisted server config + session. Server URL/anon key default to BuildConfig
 * but can be overridden at runtime (e.g. point a real device at the PC's LAN IP).
 */
class AppPrefs(context: Context) {
    private val sp = context.getSharedPreferences("recycle_prefs", Context.MODE_PRIVATE)

    init {
        // Migration: older installs persisted the local-dev default server
        // (http://10.0.2.2:54321), which is unreachable from a real device and
        // silently breaks every cloud call. Reset to the BuildConfig (cloud)
        // defaults; the stale session is cleared too so the user re-logins.
        if (sp.getString(KEY_URL, null)?.startsWith("http://10.0.2.2") == true) {
            sp.edit()
                .remove(KEY_URL).remove(KEY_ANON)
                .remove(KEY_TOKEN).remove(KEY_UID).remove(KEY_REFRESH)
                .apply()
        }
        // Sessions persisted by pre-refresh builds have no refresh token; they
        // die within an hour and surface raw "JWT expired" errors forever.
        // Drop them once so the app simply asks for a fresh login.
        if (sp.getString(KEY_TOKEN, null) != null && sp.getString(KEY_REFRESH, null) == null) {
            sp.edit().remove(KEY_TOKEN).remove(KEY_UID).apply()
        }
    }

    var serverUrl: String
        get() = sp.getString(KEY_URL, null)?.takeIf { it.isNotBlank() } ?: BuildConfig.DEFAULT_SUPABASE_URL
        set(v) { sp.edit().putString(KEY_URL, v).apply() }

    var anonKey: String
        get() = sp.getString(KEY_ANON, null)?.takeIf { it.isNotBlank() } ?: BuildConfig.DEFAULT_SUPABASE_ANON_KEY
        set(v) { sp.edit().putString(KEY_ANON, v).apply() }

    var token: String?
        get() = sp.getString(KEY_TOKEN, null)
        set(v) { sp.edit().putString(KEY_TOKEN, v).apply() }

    var userId: String?
        get() = sp.getString(KEY_UID, null)
        set(v) { sp.edit().putString(KEY_UID, v).apply() }

    var refreshToken: String?
        get() = sp.getString(KEY_REFRESH, null)
        set(v) { sp.edit().putString(KEY_REFRESH, v).apply() }

    fun clearSession() { sp.edit().remove(KEY_TOKEN).remove(KEY_UID).remove(KEY_REFRESH).apply() }

    fun newClient(): SupabaseClient {
        val c = SupabaseClient(serverUrl, anonKey)
        c.setSession(token, userId, refreshToken)
        c.onSessionRefreshed = { at, rt, uid ->
            token = at; refreshToken = rt; userId = uid
        }
        // Refresh failed -> the persisted session is dead. Wipe it so the next
        // cold start lands on login, and signal live screens to bounce now.
        c.onSessionExpired = {
            clearSession()
            SessionEvents.notifyExpired()
        }
        return c
    }

    private companion object {
        const val KEY_URL = "url"; const val KEY_ANON = "anon"
        const val KEY_TOKEN = "token"; const val KEY_UID = "uid"
        const val KEY_REFRESH = "refresh"
    }
}
