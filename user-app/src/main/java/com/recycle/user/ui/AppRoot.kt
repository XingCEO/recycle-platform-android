package com.recycle.user.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.recycle.core.net.AppPrefs
import com.recycle.core.net.SessionEvents
import com.recycle.user.ui.login.LoginScreen
import com.recycle.user.ui.main.MainScreen

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val prefs = remember { AppPrefs(context) }
    val storeOwner = LocalViewModelStoreOwner.current
    var loggedIn by remember { mutableStateOf(prefs.token != null && prefs.userId != null) }

    // Tear the session down everywhere: wipe persisted tokens, return to login,
    // and drop the Activity-scoped screen ViewModels — their in-memory clients
    // still hold the old (now dead) session, so a later login must rebuild them
    // against the fresh one. Flip the flag first so the screens leave the
    // composition before their ViewModels are cleared.
    val tearDown: () -> Unit = {
        prefs.clearSession()
        loggedIn = false
        storeOwner?.viewModelStore?.clear()
    }

    // Auth expired (refresh token dead): AppPrefs has already wiped the persisted
    // session — bounce to login instead of looping on "JWT expired" forever.
    LaunchedEffect(Unit) {
        SessionEvents.expired.collect { tearDown() }
    }

    RecycleTheme {
        if (loggedIn) {
            MainScreen(
                prefs = prefs,
                onLogout = tearDown,
            )
        } else {
            LoginScreen(
                prefs = prefs,
                onLoginSuccess = { loggedIn = true }
            )
        }
    }
}
