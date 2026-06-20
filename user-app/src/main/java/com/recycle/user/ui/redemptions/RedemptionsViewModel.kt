package com.recycle.user.ui.redemptions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recycle.core.model.Redemption
import com.recycle.core.net.ApiException
import com.recycle.core.net.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RedemptionsUiState(
    val redemptions: List<Redemption> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class RedemptionsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPrefs(app)
    private val client = prefs.newClient()
    private val _state = MutableStateFlow(RedemptionsUiState())
    val state: StateFlow<RedemptionsUiState> = _state

    init { load() }

    fun load() {
        val uid = prefs.userId ?: return
        // Shimmer only on the very first load; later tab entries refresh silently.
        _state.value = _state.value.copy(loading = _state.value.redemptions.isEmpty(), error = null)
        viewModelScope.launch {
            try {
                val list = client.listMyRedemptions(uid)
                _state.value = _state.value.copy(loading = false, redemptions = list)
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "載入失敗")
            }
        }
    }
}
