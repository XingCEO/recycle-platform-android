package com.recycle.user.ui.store

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.recycle.core.model.RedeemResult
import com.recycle.core.model.StoreItem
import com.recycle.core.net.ApiException
import com.recycle.core.net.AppPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StoreUiState(
    val items: List<StoreItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val confirmItem: StoreItem? = null,
    val redeeming: Boolean = false,
    val redeemResult: RedeemResult? = null,
    val redeemError: String? = null,
)

class StoreViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = AppPrefs(app)
    private val client = prefs.newClient()
    private val _state = MutableStateFlow(StoreUiState())
    val state: StateFlow<StoreUiState> = _state

    init { load() }

    fun load() {
        // Shimmer only on the very first load; later tab entries refresh silently.
        _state.value = _state.value.copy(loading = _state.value.items.isEmpty(), error = null)
        viewModelScope.launch {
            try {
                val items = client.listStoreItems()
                _state.value = _state.value.copy(loading = false, items = items)
            } catch (e: ApiException) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "載入失敗")
            }
        }
    }

    fun requestRedeem(item: StoreItem) {
        _state.value = _state.value.copy(confirmItem = item, redeemError = null, redeemResult = null)
    }

    fun cancelRedeem() {
        _state.value = _state.value.copy(confirmItem = null)
    }

    fun confirmRedeem() {
        val item = _state.value.confirmItem ?: return
        _state.value = _state.value.copy(redeeming = true, confirmItem = null, redeemError = null)
        viewModelScope.launch {
            try {
                val result = client.redeemItem(item.id)
                _state.value = _state.value.copy(redeeming = false, redeemResult = result)
                load() // stock changed — refresh the list behind the dialog
            } catch (e: ApiException) {
                val msg = when (e.message) {
                    "insufficient_points" -> "點數不足"
                    "out_of_stock" -> "已售完"
                    else -> e.message ?: "兌換失敗"
                }
                _state.value = _state.value.copy(redeeming = false, redeemError = msg)
            } catch (e: Exception) {
                _state.value = _state.value.copy(redeeming = false, redeemError = e.message ?: "兌換失敗")
            }
        }
    }

    fun dismissResult() {
        _state.value = _state.value.copy(redeemResult = null, redeemError = null)
    }
}
