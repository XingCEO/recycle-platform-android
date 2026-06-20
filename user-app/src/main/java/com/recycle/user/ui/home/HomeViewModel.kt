package com.recycle.user.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recycle.core.model.Profile
import com.recycle.core.net.ApiException
import com.recycle.core.net.AppPrefs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
    val profile: Profile? = null,
    val qrToken: String? = null,
    val recordCount: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPrefs(app)
    private val client = prefs.newClient()
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state
    private var refreshJob: Job? = null
    private var pointsJob: Job? = null

    init {
        loadAll()
        startAutoRefresh()
        startPointsSync()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val uid = prefs.userId ?: return@launch
                val profile = client.getProfile(uid)
                val token = client.issueUserToken()
                val records = try { client.listMyRecords(uid) } catch (e: Exception) { emptyList() }
                _state.value = _state.value.copy(
                    loading = false, profile = profile, qrToken = token,
                    recordCount = records.count { it.status == "ok" },
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "載入失敗")
            }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            try {
                val token = client.issueUserToken()
                _state.value = _state.value.copy(qrToken = token)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "刷新失敗")
            }
            syncProfile()
        }
    }

    /** Re-fetch balance + record count so awards show up without re-opening the app. */
    private suspend fun syncProfile() {
        val uid = prefs.userId ?: return
        try {
            val profile = client.getProfile(uid)
            val records = try { client.listMyRecords(uid) } catch (e: Exception) { null }
            _state.value = _state.value.copy(
                profile = profile,
                recordCount = records?.count { it.status == "ok" } ?: _state.value.recordCount,
            )
        } catch (_: Exception) {}
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(4 * 60 * 1000L) // 4 minutes
                try {
                    val token = client.issueUserToken()
                    _state.value = _state.value.copy(qrToken = token)
                } catch (_: Exception) {}
            }
        }
    }

    private fun startPointsSync() {
        pointsJob?.cancel()
        pointsJob = viewModelScope.launch {
            while (isActive) {
                delay(8_000L)
                syncProfile()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        pointsJob?.cancel()
    }
}
