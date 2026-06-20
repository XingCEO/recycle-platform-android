package com.recycle.core.net

import com.recycle.core.model.AiClassifyResult
import com.recycle.core.model.AuthResponse
import com.recycle.core.model.AwardResult
import com.recycle.core.model.Barcode
import com.recycle.core.model.Profile
import com.recycle.core.model.RecycleRecord
import com.recycle.core.model.RedeemResult
import com.recycle.core.model.Redemption
import com.recycle.core.model.MarkUsedResult
import com.recycle.core.model.StoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ApiException(val code: Int, message: String) : Exception(message)

/**
 * Thin coroutine wrapper over Supabase REST (PostgREST) + GoTrue auth.
 * All mutations go through RPC; reads go through PostgREST tables (RLS-gated).
 */
class SupabaseClient(
    baseUrl: String,
    anonKey: String,
) {
    private var baseUrl: String = baseUrl.trimEnd('/')
    private var anonKey: String = anonKey
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val mediaJson = "application/json; charset=utf-8".toMediaType()
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    var accessToken: String? = null
        private set
    var userId: String? = null
        private set
    var refreshToken: String? = null
        private set

    /** Invoked after a successful token refresh so the app layer can persist it. */
    var onSessionRefreshed: ((accessToken: String, refreshToken: String?, userId: String?) -> Unit)? = null

    /**
     * Invoked once when the session is unrecoverable (refresh token missing,
     * expired, or revoked). The app layer wipes the persisted session and
     * navigates back to login instead of looping on "JWT expired" forever.
     */
    var onSessionExpired: (() -> Unit)? = null

    fun setConfig(url: String, anon: String) { baseUrl = url.trimEnd('/'); anonKey = anon }
    fun setSession(token: String?, uid: String?, refresh: String? = null) {
        accessToken = token; userId = uid; refreshToken = refresh
    }
    fun clearSession() { accessToken = null; userId = null; refreshToken = null }

    private fun req(path: String): Request.Builder =
        Request.Builder()
            .url(baseUrl + path)
            .header("apikey", anonKey)
            .header("Authorization", "Bearer " + (accessToken ?: anonKey))

    private fun execute(request: Request): String {
        http.newCall(request).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw ApiException(resp.code, extractError(text, resp.code))
            return text
        }
    }

    /**
     * Like [execute], but on 401 (expired access token — GoTrue access tokens live
     * ~1 hour) refreshes the session once and retries with the new token.
     */
    private fun executeAuthed(request: Request): String =
        try {
            execute(request)
        } catch (e: ApiException) {
            if (e.code != 401) throw e
            refreshSession()
            execute(
                request.newBuilder()
                    .header("Authorization", "Bearer " + (accessToken ?: anonKey))
                    .build(),
            )
        }

    /**
     * The refresh token is gone or the server rejected it — there is no way to
     * recover this session. Wipe in-memory tokens and fire [onSessionExpired]
     * (which clears the persisted session and bounces the UI to login), then
     * surface a clean 401. Always throws.
     */
    private fun expireSession(): Nothing {
        clearSession()
        onSessionExpired?.invoke()
        throw ApiException(401, "登入已過期，請重新登入")
    }

    @Synchronized
    private fun refreshSession() {
        val rt = refreshToken ?: expireSession()
        val body = buildJsonObject { put("refresh_token", rt) }
        val request = Request.Builder()
            .url("$baseUrl/auth/v1/token?grant_type=refresh_token")
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
            .post(postBody(body))
            .build()
        val auth = try {
            json.decodeFromString(AuthResponse.serializer(), execute(request))
        } catch (e: Exception) {
            expireSession()
        }
        accessToken = auth.access_token
        refreshToken = auth.refresh_token ?: rt
        userId = auth.user.id
        onSessionRefreshed?.invoke(auth.access_token, refreshToken, userId)
    }

    private fun extractError(body: String, code: Int): String =
        try {
            val o = json.parseToJsonElement(body).jsonObject
            (o["message"] ?: o["error_description"] ?: o["msg"] ?: o["error"])
                ?.jsonPrimitive?.contentOrNull ?: "HTTP $code"
        } catch (e: Exception) {
            if (body.isNotBlank()) body else "HTTP $code"
        }

    private fun postBody(obj: JsonObject) =
        json.encodeToString(JsonObject.serializer(), obj).toRequestBody(mediaJson)

    // ---- Auth ----------------------------------------------------------------
    suspend fun signIn(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        val body = buildJsonObject { put("email", email); put("password", password) }
        val request = Request.Builder()
            .url("$baseUrl/auth/v1/token?grant_type=password")
            .header("apikey", anonKey)
            .header("Content-Type", "application/json")
            .post(postBody(body))
            .build()
        val auth = json.decodeFromString(AuthResponse.serializer(), execute(request))
        accessToken = auth.access_token
        refreshToken = auth.refresh_token
        userId = auth.user.id
        auth
    }

    // ---- generic helpers -----------------------------------------------------
    private suspend fun rpcRaw(fn: String, params: JsonObject): String = withContext(Dispatchers.IO) {
        executeAuthed(req("/rest/v1/rpc/$fn").header("Content-Type", "application/json").post(postBody(params)).build())
    }

    private suspend fun getRaw(path: String): String = withContext(Dispatchers.IO) {
        executeAuthed(req(path).get().build())
    }

    // ---- typed RPCs ----------------------------------------------------------
    suspend fun issueUserToken(): String {
        val text = rpcRaw("issue_user_token", buildJsonObject {})
        return json.parseToJsonElement(text).jsonPrimitive.content
    }

    suspend fun recycleAward(
        userToken: String,
        method: String,
        barcode: String? = null,
        category: String? = null,
        aiConfidence: Double? = null,
    ): AwardResult {
        val params = buildJsonObject {
            put("p_user_token", userToken)
            put("p_method", method)
            put("p_barcode", barcode)
            put("p_category", category)
            if (aiConfidence != null) put("p_ai_confidence", aiConfidence) else put("p_ai_confidence", JsonNull)
        }
        return json.decodeFromString(AwardResult.serializer(), rpcRaw("recycle_award", params))
    }

    suspend fun redeemItem(itemId: String): RedeemResult {
        val params = buildJsonObject { put("p_item_id", itemId) }
        return json.decodeFromString(RedeemResult.serializer(), rpcRaw("redeem_item", params))
    }

    suspend fun redeemMarkUsed(code: String): MarkUsedResult {
        val params = buildJsonObject { put("p_code", code) }
        return json.decodeFromString(MarkUsedResult.serializer(), rpcRaw("redeem_mark_used", params))
    }

    suspend fun adminStats(): JsonObject =
        json.parseToJsonElement(rpcRaw("admin_stats", buildJsonObject {})).jsonObject

    // ---- table reads (RLS-gated) ---------------------------------------------
    suspend fun getProfile(uid: String): Profile? =
        json.decodeFromString(ListSerializer(Profile.serializer()),
            getRaw("/rest/v1/profiles?id=eq.$uid&select=*")).firstOrNull()

    suspend fun getBarcode(barcode: String): Barcode? =
        json.decodeFromString(ListSerializer(Barcode.serializer()),
            getRaw("/rest/v1/barcodes?barcode=eq.$barcode&select=*")).firstOrNull()

    suspend fun listStoreItems(): List<StoreItem> =
        json.decodeFromString(ListSerializer(StoreItem.serializer()),
            getRaw("/rest/v1/store_items?is_active=eq.true&order=cost_points.asc&select=*"))

    suspend fun listMyRecords(uid: String): List<RecycleRecord> =
        json.decodeFromString(ListSerializer(RecycleRecord.serializer()),
            getRaw("/rest/v1/recycle_records?user_id=eq.$uid&order=created_at.desc&select=*"))

    suspend fun listMyRedemptions(uid: String): List<Redemption> =
        json.decodeFromString(ListSerializer(Redemption.serializer()),
            getRaw("/rest/v1/redemptions?user_id=eq.$uid&order=created_at.desc&select=*"))

    // ---- Edge Functions ------------------------------------------------------
    suspend fun classifyImage(imageB64: String): AiClassifyResult = withContext(Dispatchers.IO) {
        val body = buildJsonObject { put("image_b64", imageB64) }
        val request = req("/functions/v1/classify-image")
            .header("Content-Type", "application/json")
            .post(postBody(body))
            .build()
        json.decodeFromString(AiClassifyResult.serializer(), executeAuthed(request))
    }
}
