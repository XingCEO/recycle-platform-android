package com.recycle.user.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import android.app.Activity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.recycle.core.net.AppPrefs
import com.recycle.user.ui.home.HomeScreen
import com.recycle.user.ui.records.RecordsScreen
import com.recycle.user.ui.redemptions.RedemptionsScreen
import com.recycle.user.ui.store.StoreScreen

private data class NavDest(
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
)

@Composable
fun MainScreen(prefs: AppPrefs, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Home (tab 0) draws a dark green hero behind the status bar -> white icons;
    // the other tabs sit on a light surface -> dark icons.
    val view = LocalView.current
    LaunchedEffect(selectedTab) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = selectedTab != 0
    }

    val destinations = remember {
        listOf(
            NavDest("首頁", Icons.Filled.Home, Icons.Outlined.Home),
            NavDest("回收紀錄", Icons.Filled.Recycling, Icons.Outlined.Recycling),
            NavDest("商店", Icons.Filled.Storefront, Icons.Outlined.Storefront),
            NavDest("兌換紀錄", Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard),
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // Let each screen draw its gradient behind the status bar (edge-to-edge);
        // screens re-inset their content with statusBarsPadding().
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                destinations.forEachIndexed { index, dest ->
                    val selected = selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                if (selected) dest.selectedIcon else dest.icon,
                                contentDescription = dest.label,
                            )
                        },
                        label = {
                            Text(
                                dest.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                (fadeIn(tween(320)) togetherWith fadeOut(tween(180)))
                    .using(SizeTransform(clip = false))
            },
            label = "tabContent",
        ) { tab ->
            when (tab) {
                0 -> HomeScreen(prefs = prefs, onLogout = onLogout, modifier = Modifier.padding(innerPadding))
                1 -> RecordsScreen(prefs = prefs, modifier = Modifier.padding(innerPadding))
                2 -> StoreScreen(prefs = prefs, modifier = Modifier.padding(innerPadding))
                3 -> RedemptionsScreen(prefs = prefs, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
