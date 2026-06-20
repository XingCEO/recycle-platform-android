package com.recycle.vendor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recycle.core.model.AwardResult
import com.recycle.core.model.Category
import com.recycle.core.net.ApiException
import com.recycle.core.net.AppPrefs
import com.recycle.core.net.SessionEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen { LOGIN, HOME, SCAN_USER, BARCODE, AI }

data class VendorUi(
    val screen: Screen = Screen.LOGIN,
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val sessionCount: Int = 0,
    val sessionPoints: Int = 0,
)

class VendorViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPrefs(app)
    internal val client = prefs.newClient()
    private var userToken: String? = null

    // Lazily loaded so the TFLite model only initialises when AI mode is first used.
    val classifier: TfliteClassifier by lazy { TfliteClassifier(getApplication()) }

    private val _ui = MutableStateFlow(VendorUi(screen = if (prefs.token != null) Screen.HOME else Screen.LOGIN))
    val ui: StateFlow<VendorUi> = _ui.asStateFlow()

    init {
        // Auth expired (refresh token dead): prefs already wiped by AppPrefs —
        // force re-login instead of looping 401s on every scan/award.
        viewModelScope.launch {
            SessionEvents.expired.collect {
                prefs.clearSession(); client.clearSession(); userToken = null
                _ui.value = VendorUi(screen = Screen.LOGIN, error = "登入已過期，請重新登入")
            }
        }
    }

    fun serverUrl(): String = prefs.serverUrl
    fun setServer(url: String) { prefs.serverUrl = url; client.setConfig(prefs.serverUrl, prefs.anonKey) }

    fun login(email: String, pw: String) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                client.setConfig(prefs.serverUrl, prefs.anonKey)
                val auth = client.signIn(email, pw)
                val profile = client.getProfile(auth.user.id)
                if (profile?.role != "vendor" && profile?.role != "admin") {
                    client.clearSession()
                    _ui.update { it.copy(loading = false, error = "此帳號非廠商身分") }
                    return@launch
                }
                prefs.token = client.accessToken
                prefs.userId = client.userId
                prefs.refreshToken = client.refreshToken
                _ui.update { it.copy(loading = false, screen = Screen.HOME) }
            } catch (e: ApiException) {
                _ui.update { it.copy(loading = false, error = e.message ?: "登入失敗") }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = e.message ?: "登入失敗") }
            }
        }
    }

    fun logout() {
        prefs.clearSession(); client.clearSession(); userToken = null
        _ui.value = VendorUi(screen = Screen.LOGIN)
    }

    fun goScanUser() = _ui.update { it.copy(screen = Screen.SCAN_USER, error = null, message = null) }
    fun goHome() { userToken = null; _ui.update { it.copy(screen = Screen.HOME, message = null, error = null) } }
    fun goBarcode() = _ui.update { it.copy(screen = Screen.BARCODE, message = null, error = null) }
    fun goAi() = _ui.update { it.copy(screen = Screen.AI, message = null, error = null) }

    fun onUserScanned(token: String) {
        userToken = token
        _ui.update {
            it.copy(screen = Screen.BARCODE, message = "使用者已識別，開始回收", error = null,
                sessionCount = 0, sessionPoints = 0)
        }
    }

    private var lastBarcode: String? = null
    private var lastBarcodeAt = 0L
    fun onBarcode(barcode: String) {
        val now = System.currentTimeMillis()
        if (barcode == lastBarcode && now - lastBarcodeAt < 2500) return
        lastBarcode = barcode; lastBarcodeAt = now
        val token = userToken ?: return
        viewModelScope.launch {
            try { applyResult(client.recycleAward(token, "barcode", barcode = barcode)) }
            catch (e: ApiException) { _ui.update { it.copy(error = mapErr(e.message), message = null) } }
            catch (e: Exception) { _ui.update { it.copy(error = e.message, message = null) } }
        }
    }

    fun onAiResult(category: String, conf: Double) {
        val token = userToken ?: return
        viewModelScope.launch {
            try { applyResult(client.recycleAward(token, "ai", category = category, aiConfidence = conf)) }
            catch (e: ApiException) { _ui.update { it.copy(error = mapErr(e.message), message = null) } }
            catch (e: Exception) { _ui.update { it.copy(error = e.message, message = null) } }
        }
    }

    private fun applyResult(r: AwardResult) {
        if (r.status == "rejected_junk") {
            _ui.update { it.copy(message = "⚠ 不可回收（垃圾條碼），已記錄", error = null) }
        } else {
            _ui.update {
                it.copy(
                    message = "✓ ${Category.label(r.category ?: "")} +${r.points_awarded} 點（使用者餘額 ${r.new_balance}）",
                    sessionCount = it.sessionCount + 1,
                    sessionPoints = it.sessionPoints + r.points_awarded,
                    error = null,
                )
            }
        }
    }

    private fun mapErr(m: String?): String = when (m) {
        "duplicate_scan" -> "重複掃描，請稍候再試"
        "unknown_barcode" -> "查無此條碼"
        "token_expired", "invalid_token", "wrong_token" -> "使用者識別已過期，請重新掃描使用者 QR"
        "low_confidence" -> "AI 信心低於 50%，依規定不可回收"
        "not_a_vendor" -> "權限不足"
        else -> m ?: "發生錯誤"
    }

    fun clearMessage() = _ui.update { it.copy(message = null, error = null) }
}
