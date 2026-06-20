@file:OptIn(ExperimentalMaterial3Api::class)

package com.recycle.vendor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.SportsBar
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.foundation.clickable
import com.recycle.core.model.Category
import com.recycle.vendor.ui.theme.Amber
import com.recycle.vendor.ui.theme.Emerald
import com.recycle.vendor.ui.theme.Emerald700
import com.recycle.vendor.ui.theme.Emerald900
import com.recycle.vendor.ui.theme.Teal
import com.recycle.vendor.ui.theme.ButtonGradient
import com.recycle.vendor.ui.theme.HeroGradient
import com.recycle.vendor.ui.theme.ScreenGradient
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VendorApp(vm: VendorViewModel = viewModel()) {
    val ui by vm.ui.collectAsState()
    Box(Modifier.fillMaxSize().background(ScreenGradient)) {
        Crossfade(targetState = ui.screen, label = "screen") { screen ->
            when (screen) {
                Screen.LOGIN -> LoginScreen(vm, ui)
                Screen.HOME -> HomeScreen(vm, ui)
                Screen.SCAN_USER -> ScanUserScreen(vm)
                Screen.BARCODE, Screen.AI -> RecycleScreen(vm, ui)
            }
        }
    }
}

/* ---------- shared bits ---------- */

@Composable
private fun LogoBadge(size: Int = 72) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(HeroGradient),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Recycling, null, tint = Color.White, modifier = Modifier.size((size * 0.55).dp))
    }
}

@Composable
private fun GradientButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Box(
            Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                .background(if (enabled) ButtonGradient else Brush.linearGradient(listOf(Color(0xFFB6C5BD), Color(0xFFB6C5BD)))),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
private fun StatusBanner(error: String?, message: String?) {
    val show = error != null || message != null
    AnimatedVisibility(show, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        val isErr = error != null
        Surface(
            color = if (isErr) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    if (isErr) Icons.Rounded.Warning else Icons.Rounded.CheckCircle, null,
                    tint = if (isErr) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
                Text(error ?: message ?: "", color = if (isErr) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* ---------- screens ---------- */

@Composable
private fun LoginScreen(vm: VendorViewModel, ui: VendorUi) {
    var email by remember { mutableStateOf("vendor1@demo.test") }
    var pw by remember { mutableStateOf("password123") }
    var server by remember { mutableStateOf(vm.serverUrl()) }
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Emerald900, Emerald700, Teal)
                )
            )
    ) {
        // decorative circle top-right
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 24.dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))
            LogoBadge(88)
            Spacer(Modifier.height(16.dp))
            Text("回收廠商", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("廠商檢測 · 掃碼發點", color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(32.dp))
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("登入帳號", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(email, { email = it }, label = { Text("帳號 Email") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(pw, { pw = it }, label = { Text("密碼") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(server, { server = it }, label = { Text("伺服器網址") }, singleLine = true, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    StatusBanner(ui.error, null)
                    GradientButton(if (ui.loading) "登入中…" else "登入", Icons.Rounded.Bolt, enabled = !ui.loading) {
                        vm.setServer(server.trim()); vm.login(email.trim(), pw)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("示範帳號：vendor1@demo.test / password123", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(6.dp))
            Text("v1.1 · 雲端 AI 辨識版", fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeScreen(vm: VendorViewModel, ui: VendorUi) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // hero
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)).background(HeroGradient)) {
            Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(140.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))
            Column(Modifier.statusBarsPadding().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LogoBadge(40)
                        Text("回收廠商", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    TextButton(onClick = vm::logout) {
                        Icon(Icons.Rounded.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp)); Text("登出", color = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("準備好開始回收檢測", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("掃描使用者 QR → 檢測投放 → 自動發點", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GradientButton("掃描使用者 QR", Icons.Rounded.QrCodeScanner) { vm.goScanUser() }

            InfoCard("回收流程", Icons.Rounded.Recycling) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    FlowStep("出示掃碼", Icons.Rounded.QrCodeScanner)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    FlowStep("檢測辨識", Icons.Rounded.PhotoCamera)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    FlowStep("自動發點", Icons.Rounded.Savings)
                }
            }

            InfoCard("點數對照", Icons.Rounded.Savings) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RateTile("寶特瓶", "10", Icons.Rounded.LocalDrink, Emerald, Modifier.weight(1f))
                    RateTile("鐵鋁罐", "8", Icons.Rounded.SportsBar, Teal, Modifier.weight(1f))
                    RateTile("鋁箔包", "5", Icons.Rounded.Inventory2, Amber, Modifier.weight(1f))
                }
            }

            InfoCard("小提醒", Icons.Rounded.TipsAndUpdates) {
                TipRow("使用者 QR 每 5 分鐘更新，過期請重新掃描")
                TipRow("掃到垃圾條碼會自動擋下並記錄")
                TipRow("AI 信心 50–70% 需人工確認；低於 50% 不可回收")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 3.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    )
                )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ButtonGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                content()
            }
        }
    }
}

@Composable
private fun FlowStep(label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(46.dp).clip(CircleShape).background(ButtonGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RateTile(name: String, pts: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.10f)).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(pts, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(" 點", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TipRow(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.padding(top = 6.dp).size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScanFrameOverlay() {
    val t = rememberInfiniteTransition(label = "scan")
    val pos by t.animateFloat(0.05f, 0.95f, infiniteRepeatable(tween(1700), RepeatMode.Reverse), label = "pos")
    Box(Modifier.fillMaxSize().padding(28.dp)) {
        Box(Modifier.fillMaxSize().border(BorderStroke(3.dp, Color.White.copy(alpha = 0.9f)), RoundedCornerShape(24.dp)))
        Box(Modifier.fillMaxHeight(pos)) {
            Box(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, com.recycle.vendor.ui.theme.Emerald, Color.Transparent)))
            )
        }
    }
}

@Composable
private fun ScanUserScreen(vm: VendorViewModel) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = vm::goHome) { Text("← 返回") }
            Spacer(Modifier.width(4.dp))
            Text("掃描使用者 QR", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
        ) {
            Box(Modifier.fillMaxSize()) {
                WithCameraPermission { BarcodeCamera(onBarcode = { vm.onUserScanned(it) }) }
                ScanFrameOverlay()
            }
        }
        Text("對準使用者 App 首頁的 QR 碼", Modifier.fillMaxWidth().padding(bottom = 20.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecycleScreen(vm: VendorViewModel, ui: VendorUi) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // header with session chips
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)).background(HeroGradient)) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = vm::goHome) { Text("✓ 完成", color = Color.White, fontWeight = FontWeight.SemiBold) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SessionChip("${ui.sessionCount} 件")
                        SessionChip("${ui.sessionPoints} 點")
                    }
                }
            }
        }
        // segmented tabs
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ModeTab("條碼", Icons.Rounded.QrCode2, ui.screen == Screen.BARCODE, Modifier.weight(1f)) { vm.goBarcode() }
            ModeTab("AI 辨識", Icons.Rounded.PhotoCamera, ui.screen == Screen.AI, Modifier.weight(1f)) { vm.goAi() }
        }
        StatusBanner(ui.error, ui.message)
        Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            Card(shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(6.dp), modifier = Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) {
                    if (ui.screen == Screen.BARCODE) {
                        WithCameraPermission { BarcodeCamera(onBarcode = vm::onBarcode) }
                        ScanFrameOverlay()
                    } else {
                        AiPane(vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionChip(text: String) {
    Surface(color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(50)) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 6.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ModeTab(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .then(
                if (selected)
                    Modifier.background(ButtonGradient)
                else
                    Modifier
                        .background(Color.White)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected) Color.White else MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AiPane(vm: VendorViewModel) {
    val holder = remember { FrameHolder() }
    var guess by remember { mutableStateOf<Pair<String, Double>?>(null) }
    var guessSource by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            WithCameraPermission { AiCamera(holder = holder) }
        }
        Surface(color = Color.White, shadowElevation = 8.dp, tonalElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GradientButton(if (busy) "辨識中…" else "辨識物品", Icons.Rounded.PhotoCamera, enabled = !busy) {
                    val bmp = holder.bitmap
                    if (bmp != null && !busy) {
                        busy = true
                        scope.launch {
                            var res: Pair<String, Double> = Pair("", 0.0)
                            var src = "離線模型"
                            try {
                                // Downscale to longest side <= 800
                                val scaled = withContext(Dispatchers.Default) {
                                    val w = bmp.width; val h = bmp.height
                                    val max = maxOf(w, h)
                                    if (max > 800) {
                                        val scale = 800f / max
                                        Bitmap.createScaledBitmap(bmp, (w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1), true)
                                    } else bmp
                                }
                                val b64 = withContext(Dispatchers.Default) {
                                    val baos = ByteArrayOutputStream()
                                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, baos)
                                    Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                                }
                                val aiResult = vm.client.classifyImage(b64)
                                val conf = if (aiResult.category == "other") 0.0 else aiResult.confidence
                                res = Pair(aiResult.category, conf)
                                src = "雲端 AI"
                            } catch (e: Exception) {
                                // Offline fallback
                                android.util.Log.w("AiPane", "cloud classify failed", e)
                                res = withContext(Dispatchers.Default) { vm.classifier.classify(bmp) }
                                src = "離線模型（雲端連線失敗）"
                            }
                            guess = res; guessSource = src; busy = false
                        }
                    }
                }
                val g = guess
                AnimatedVisibility(g != null, enter = fadeIn() + expandVertically()) {
                    if (g != null) {
                        val conf = g.second
                        val widthFrac by animateFloatAsState(conf.toFloat().coerceIn(0f, 1f), label = "conf")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Rounded.Bolt, null, tint = MaterialTheme.colorScheme.tertiary)
                                Text(
                                    if (g.first == "other") "未辨識出回收物"
                                    else "辨識：${Category.label(g.first)}　信心 ${(conf * 100).toInt()}%",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (guessSource.isNotEmpty()) {
                                Text("來源：$guessSource", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            LinearProgressIndicator(progress = { widthFrac }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)))
                            if (g.first == "other" || conf < 0.5) {
                                // Business rule: below 50% confidence the item cannot be recycled.
                                Text(
                                    if (g.first == "other")
                                        "鏡頭中未發現寶特瓶／鐵鋁罐／鋁箔包，請對準回收物重新辨識。"
                                    else
                                        "信心低於 50%，依規定不可回收。請重新拍攝，或改用條碼掃描。",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                )
                            } else if (conf >= 0.7) {
                                GradientButton("確認送出：${Category.label(g.first)}", Icons.Rounded.CheckCircle) { vm.onAiResult(g.first, conf); guess = null }
                            } else {
                                Text("信心 50–70%，請人工確認類別：", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Category.ALL.forEach { c ->
                                        Button(onClick = { vm.onAiResult(c, conf); guess = null }, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) {
                                            Text(Category.label(c))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
