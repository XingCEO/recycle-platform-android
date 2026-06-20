package com.recycle.user.ui

import androidx.compose.runtime.Composable
import com.recycle.user.ui.theme.RecycleAppTheme

/** Backwards-compatible entry point; delegates to the premium-eco app theme. */
@Composable
fun RecycleTheme(content: @Composable () -> Unit) {
    RecycleAppTheme(content = content)
}
