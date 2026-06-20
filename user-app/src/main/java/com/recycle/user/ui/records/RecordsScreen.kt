package com.recycle.user.ui.records

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
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
import com.recycle.core.model.Category
import com.recycle.core.model.RecycleRecord
import com.recycle.core.model.formatTaipei
import com.recycle.core.net.AppPrefs
import com.recycle.user.ui.components.EmptyState
import com.recycle.user.ui.components.ShimmerBox
import com.recycle.user.ui.components.StatusChip
import com.recycle.user.ui.theme.BadgeGradient
import com.recycle.user.ui.theme.Emerald100
import com.recycle.user.ui.theme.Emerald700

@Composable
fun RecordsScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier,
    vm: RecordsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // The tab ViewModel outlives tab switches (activity-scoped), so refresh on
    // every entry — otherwise new recycles never show without an app restart.
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
        ScreenHeader(
            icon = Icons.Filled.Recycling,
            title = "回收紀錄",
            subtitle = "你的每一次回收都在改變世界",
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(76.dp),
                        shape = RoundedCornerShape(20.dp),
                    )
                }
            }
            state.error != null -> ErrorPanel(state.error!!)
            state.records.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Outlined.History,
                    text = "尚無回收紀錄",
                    subtitle = "開始回收即可累積點數",
                )
            }
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(state.records, key = { _, r -> r.id }) { index, record ->
                    RecordCard(record, index)
                }
            }
        }
    }
}

@Composable
private fun RecordCard(record: RecycleRecord, index: Int) {
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
        val (catIcon, catGradient) = categoryVisual(record.category)
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
                        .background(Brush.linearGradient(catGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(catIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    val catLabel = record.category?.let { Category.label(it) } ?: "—"
                    Text(
                        catLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (record.method == "barcode") Icons.Filled.QrCodeScanner else Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            (if (record.method == "barcode") "條碼" else "AI 辨識") +
                                " ・ " + formatTaipei(record.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "+${record.points_awarded}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.height(4.dp))
                    when (record.status) {
                        "ok" -> StatusChip(
                            text = "正常",
                            container = Emerald100,
                            content = Emerald700,
                            icon = Icons.Filled.CheckCircle,
                        )
                        "rejected_junk" -> StatusChip(
                            text = "不可回收",
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.error,
                            icon = Icons.Filled.Block,
                        )
                        else -> StatusChip(
                            text = record.status,
                            container = MaterialTheme.colorScheme.surfaceVariant,
                            content = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun categoryVisual(category: String?): Pair<ImageVector, List<Color>> = when (category) {
    Category.PET -> Icons.Filled.LocalDrink to listOf(Color(0xFF06B6D4), Color(0xFF0EA5E9))
    Category.CAN -> Icons.Filled.Recycling to listOf(Color(0xFF64748B), Color(0xFF475569))
    Category.CARTON -> Icons.Filled.Recycling to listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
    else -> Icons.Filled.Recycling to BadgeGradient
}

@Composable
internal fun ScreenHeader(icon: ImageVector, title: String, subtitle: String) {
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
internal fun ErrorPanel(message: String) {
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
