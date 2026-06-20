package com.recycle.user.ui.store

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.recycle.core.model.StoreItem
import com.recycle.core.net.AppPrefs
import com.recycle.core.qr.QrCodec
import com.recycle.user.ui.components.EmptyState
import com.recycle.user.ui.components.GradientButton
import com.recycle.user.ui.components.ShimmerBox
import com.recycle.user.ui.theme.Amber100
import com.recycle.user.ui.theme.Amber700
import com.recycle.user.ui.theme.BadgeGradient
import com.recycle.user.ui.theme.ButtonGradient
import com.recycle.user.ui.theme.Emerald100
import com.recycle.user.ui.theme.Emerald50
import com.recycle.user.ui.theme.Emerald700
import com.recycle.user.ui.theme.HeroGradient
import com.recycle.user.ui.theme.Ink900
import com.recycle.user.ui.theme.Slate500

@Composable
fun StoreScreen(
    prefs: AppPrefs,
    modifier: Modifier = Modifier,
    vm: StoreViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // The tab ViewModel outlives tab switches (activity-scoped), so refresh on
    // every entry to pick up stock/price changes made from the admin panel.
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(BadgeGradient)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Redeem,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(
                    "兌換商店",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "用回收點數兌換好禮",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(5) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(88.dp),
                        shape = RoundedCornerShape(22.dp),
                    )
                }
            }
            state.error != null -> ErrorPanel(state.error!!)
            state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Filled.Inventory2,
                    text = "目前沒有商品",
                    subtitle = "請稍後再回來看看",
                )
            }
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(state.items, key = { _, it -> it.id }) { index, item ->
                    StoreItemCard(
                        item = item,
                        index = index,
                        onClick = { vm.requestRedeem(item) },
                    )
                }
            }
        }
    }

    // Loading overlay while redeeming
    if (state.redeeming) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(shape = RoundedCornerShape(20.dp), shadowElevation = 8.dp) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(28.dp),
                )
            }
        }
    }

    // Confirm dialog
    state.confirmItem?.let { item ->
        AlertDialog(
            onDismissRequest = vm::cancelRedeem,
            shape = RoundedCornerShape(28.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(BadgeGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Redeem, contentDescription = null, tint = Color.White)
                }
            },
            title = { Text("確認兌換", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "兌換「${item.name}」",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(10.dp))
                    CostPill(cost = item.cost_points, large = true)
                }
            },
            confirmButton = {
                GradientButton(
                    text = "確認兌換",
                    onClick = vm::confirmRedeem,
                    gradient = ButtonGradient,
                    height = 48.dp,
                )
            },
            dismissButton = { TextButton(onClick = vm::cancelRedeem) { Text("取消") } },
        )
    }

    // Result dialog — fully designed success card (stock AlertDialog reads
    // as a grey toned box and misaligns the pieces)
    state.redeemResult?.let { result ->
        Dialog(onDismissRequest = vm::dismissResult) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // gradient header band: check disc + title + item
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(HeroGradient))
                            .padding(top = 26.dp, bottom = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "兌換成功",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            result.item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val bitmap = remember(result.code) { QrCodec.encode(result.code, 400) }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Emerald100),
                            shadowElevation = 5.dp,
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "兌換碼 QR",
                                modifier = Modifier.padding(14.dp).size(180.dp),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald50,
                            border = BorderStroke(1.dp, Emerald100),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            ) {
                                Text(
                                    "兌換碼",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Emerald700,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    result.code,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Ink900,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            StatTile(
                                label = "花費",
                                value = "${result.cost} 點",
                                modifier = Modifier.weight(1f),
                            )
                            StatTile(
                                label = "剩餘點數",
                                value = "${result.new_balance} 點",
                                modifier = Modifier.weight(1f),
                                highlight = true,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "向回收站出示此 QR 即可核銷",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                        )
                        Spacer(Modifier.height(14.dp))
                        GradientButton(
                            text = "完成",
                            onClick = vm::dismissResult,
                            gradient = ButtonGradient,
                            modifier = Modifier.fillMaxWidth(),
                            height = 52.dp,
                        )
                    }
                }
            }
        }
    }

    // Error dialog
    state.redeemError?.let { err ->
        AlertDialog(
            onDismissRequest = vm::dismissResult,
            shape = RoundedCornerShape(28.dp),
            title = { Text("兌換失敗", style = MaterialTheme.typography.headlineSmall) },
            text = { Text(err, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = { TextButton(onClick = vm::dismissResult) { Text("確定") } },
        )
    }
}

@Composable
private fun StoreItemCard(item: StoreItem, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(360, delayMillis = index * 55)) +
            slideInVertically(
                animationSpec = tween(360, delayMillis = index * 55, easing = LinearOutSlowInEasing),
                initialOffsetY = { it / 3 },
            ),
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onClick),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(BadgeGradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Redeem,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.size(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "庫存 ${item.stock}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.size(10.dp))
                CostPill(cost = item.cost_points)
            }
        }
    }
}

@Composable
private fun CostPill(cost: Int, large: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Amber100,
        contentColor = Amber700,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = if (large) 18.dp else 12.dp,
                vertical = if (large) 10.dp else 7.dp,
            ),
        ) {
            Icon(
                Icons.Filled.Stars,
                contentDescription = null,
                modifier = Modifier.size(if (large) 20.dp else 16.dp),
            )
            Spacer(Modifier.size(5.dp))
            Text(
                "$cost 點",
                style = if (large) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlight) Emerald50 else com.recycle.user.ui.theme.Surface1,
        border = BorderStroke(1.dp, if (highlight) Emerald100 else com.recycle.user.ui.theme.OutlineSoft),
        modifier = modifier,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = if (highlight) Emerald700 else com.recycle.user.ui.theme.Slate600,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Emerald700 else Ink900,
            )
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
