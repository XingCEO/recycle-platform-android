package com.recycle.user.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.recycle.core.net.AppPrefs
import com.recycle.core.qr.QrCodec
import com.recycle.user.ui.components.ShimmerBox
import com.recycle.user.ui.theme.Emerald500
import com.recycle.user.ui.theme.PointsGradient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    prefs: AppPrefs,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val lifecycle = LocalLifecycleOwner.current

    // Refresh profile on resume
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            vm.loadAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        ) {
            HomeContent(state, vm, onLogout)
        }
        // Fixed scrim so scrolled content stays readable behind the status bar
        // (matches the hero's top gradient color; invisible when at scroll top).
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(Emerald500)
        )
    }
}

@Composable
private fun ColumnScope.HomeContent(
    state: HomeUiState,
    vm: HomeViewModel,
    onLogout: () -> Unit,
) {
        // ---- Hero header ----------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(Brush.verticalGradient(PointsGradient)),
        ) {
            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "歡迎回來",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                        Text(
                            state.profile?.display_name ?: "—",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                    ) {
                        Icon(
                            Icons.Filled.Logout,
                            contentDescription = "登出",
                            tint = Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Points
                Text(
                    "我的點數",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.EnergySavingsLeaf,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Spacer(Modifier.size(14.dp))
                    val targetPoints = state.profile?.points ?: 0
                    val animatedPoints by animateIntAsState(
                        targetValue = targetPoints,
                        animationSpec = tween(900, easing = LinearOutSlowInEasing),
                        label = "points",
                    )
                    Text(
                        "$animatedPoints",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "點",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
        }

        // ---- QR card --------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MembershipCard(state = state, onRefresh = vm::refreshToken)

            AnimatedVisibility(visible = state.error != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    Text(
                        state.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        EcoSections(
            points = state.profile?.points ?: 0,
            recordCount = state.recordCount,
        )
        Spacer(Modifier.height(24.dp))
}

@Composable
private fun EcoSections(points: Int, recordCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard(title = "環保成就", icon = Icons.Filled.Recycling) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EcoTile("回收件數", "$recordCount", "件", Icons.Filled.Recycling, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                EcoTile("約減碳", String.format("%.1f", recordCount * 0.04), "kg", Icons.Filled.Cloud, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                EcoTile("等同種樹", "${recordCount / 20}", "棵", Icons.Filled.Park, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
            Text("＊環保效益為估算值", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        val goal = if (points < 1000) 1000 else ((points / 1000) + 1) * 1000
        val frac = (points.toFloat() / goal).coerceIn(0f, 1f)
        SectionCard(title = "點數進度", icon = Icons.Filled.EmojiEvents) {
            Text("再 ${goal - points} 點達成 $goal 點目標", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            LinearProgressIndicator(progress = { frac }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)))
            Text("$points / $goal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SectionCard(title = "如何累積點數", icon = Icons.Filled.Savings) {
            GuideStep("在首頁出示個人回收碼", Icons.Filled.QrCode2)
            GuideStep("廠商掃描條碼或 AI 辨識容器", Icons.Filled.PhotoCamera)
            GuideStep("點數自動入帳，可至商店兌換", Icons.Filled.Savings)
        }

        SectionCard(title = "小提醒", icon = Icons.Filled.TipsAndUpdates) {
            TipRow("回收碼每 5 分鐘自動更新，確保安全")
            TipRow("寶特瓶請先倒空壓扁，鋁箔包屬紙容器")
            TipRow("分類正確才能順利累積點數")
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun EcoTile(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = 0.10f)).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = accent)
            Text(" $unit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GuideStep(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun TipRow(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.padding(top = 6.dp).size(6.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RefreshButton(onClick: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val interaction = remember { MutableInteractionSource() }

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(),
                ) {
                    scope.launch {
                        rotation.snapTo(0f)
                        rotation.animateTo(360f, animationSpec = tween(600))
                    }
                    onClick()
                }
                .padding(horizontal = 22.dp, vertical = 12.dp),
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation.value),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                "刷新回收碼",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MembershipCard(state: HomeUiState, onRefresh: () -> Unit) {
    val points = state.profile?.points ?: 0
    val (tierName, tierColors) = when {
        points >= 1000 -> "金卡會員" to listOf(Color(0xFF8A6D1B), Color(0xFFE6B325), Color(0xFFB8860B))
        points >= 300 -> "銀卡會員" to listOf(Color(0xFF5B6472), Color(0xFF9CA3AF), Color(0xFF6B7280))
        else -> "綠卡會員" to listOf(Color(0xFF065F46), Color(0xFF10B981), Color(0xFF0D9488))
    }
    val memberNo = (state.profile?.id ?: "").replace("-", "").uppercase().take(8).chunked(4).joinToString(" ").ifBlank { "—" }

    Surface(shape = RoundedCornerShape(28.dp), shadowElevation = 12.dp, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.background(Brush.linearGradient(tierColors))) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).size(120.dp)
                    .clip(CircleShape).background(Color.White.copy(alpha = 0.08f)),
            )
            Column(Modifier.padding(22.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Recycling, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("回收會員卡", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Surface(color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(50)) {
                        Text(tierName, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(state.profile?.display_name ?: "—", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Black)
                Text("會員編號  $memberNo", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(18.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.loading) {
                            ShimmerBox(modifier = Modifier.size(200.dp), shape = RoundedCornerShape(16.dp))
                        } else {
                            state.qrToken?.let { token ->
                                val bitmap = remember(token) { QrCodec.encode(token, 600) }
                                Image(bitmap = bitmap.asImageBitmap(), contentDescription = "會員 QR", modifier = Modifier.size(200.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("出示此碼給回收站掃描", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("可用點數", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$points", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Black)
                            Text(" 點", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 3.dp))
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.22f))
                            .clickable { onRefresh() }.padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("更新", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
