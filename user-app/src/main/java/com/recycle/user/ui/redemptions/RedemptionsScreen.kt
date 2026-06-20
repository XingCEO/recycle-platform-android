package com.recycle.user.ui.redemptions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.recycle.core.model.Redemption
import com.recycle.core.model.formatTaipei
import com.recycle.core.net.AppPrefs
import com.recycle.user.ui.components.EmptyState
import com.recycle.user.ui.components.ShimmerBox
import com.recycle.user.ui.components.StatusChip
import com.recycle.user.ui.theme.Amber100
import com.recycle.user.ui.theme.Amber700
import com.recycle.user.ui.theme.BadgeGradient

@Composable
fun RedemptionsScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier,
    vm: RedemptionsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // The tab ViewModel outlives tab switches (activity-scoped), so refresh on
    // every entry — otherwise a redemption made on the store tab never shows.
    androidx.compose.runtime.LaunchedEffect(Unit) { vm.load() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        com.recycle.user.ui.theme.Surface1,
                        com.recycle.user.ui.theme.Surface0,
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Header(
            icon = Icons.Filled.CardGiftcard,
            title = "兌換紀錄",
            subtitle = "查看你的兌換券與核銷狀態",
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(82.dp),
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
            state.error != null -> ErrorPanel(state.error!!)
            state.redemptions.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.Inbox,
                    text = "尚無兌換紀錄",
                    subtitle = "到商店兌換你的第一份好禮",
                )
            }
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(state.redemptions, key = { _, r -> r.id }) { index, r ->
                    RedemptionCard(r, index)
                }
            }
        }
    }
}

@Composable
private fun RedemptionCard(r: Redemption, index: Int) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(340, delayMillis = index * 45)) +
            slideInVertically(
                animationSpec = tween(340, delayMillis = index * 45, easing = LinearOutSlowInEasing),
                initialOffsetY = { it / 3 },
            ),
    ) {
        val used = r.status == "used"
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(BadgeGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Redeem, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "兌換碼 ${r.code}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        formatTaipei(r.created_at),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Amber100,
                        contentColor = Amber700,
                    ) {
                        Text(
                            "${r.cost_points} 點",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                when {
                    used -> StatusChip(
                        text = "已核銷",
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = Icons.Filled.TaskAlt,
                    )
                    r.status == "pending" -> StatusChip(
                        text = "待核銷",
                        container = Amber100,
                        content = Amber700,
                        icon = Icons.Filled.HourglassTop,
                    )
                    else -> StatusChip(
                        text = if (r.status == "cancelled") "已取消" else r.status,
                        container = MaterialTheme.colorScheme.surfaceVariant,
                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Brush.linearGradient(BadgeGradient)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorPanel(message: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}
