package com.recycle.user.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recycle.core.net.ApiException
import com.recycle.core.net.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "user1@demo.test",
    val password: String = "password123",
    val serverUrl: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPrefs(app)
    private val _state = MutableStateFlow(LoginUiState(serverUrl = prefs.serverUrl))
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v) }
    fun onServerUrlChange(v: String) { _state.value = _state.value.copy(serverUrl = v) }

    fun login(onSuccess: () -> Unit) {
        val s = _state.value
        val url = s.serverUrl.trim()
        if (url.isNotBlank()) prefs.serverUrl = url

        _state.value = s.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val client = prefs.newClient()
                client.signIn(s.email.trim(), s.password)
                prefs.token = client.accessToken
                prefs.userId = client.userId
                prefs.refreshToken = client.refreshToken
                _state.value = _state.value.copy(loading = false)
                onSuccess()
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "連線失敗")
            }
        }
    }
}
