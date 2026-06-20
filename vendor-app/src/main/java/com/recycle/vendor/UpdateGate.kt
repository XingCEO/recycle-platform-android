package com.recycle.vendor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.recycle.core.update.ApkInstaller
import com.recycle.core.update.UpdateInfo
import com.recycle.core.update.Updater
import kotlinx.coroutines.launch

/**
 * On launch, checks the remote manifest and — if a newer build exists — offers
 * to download and install it (sideload self-update; no Play Store). Renders
 * nothing when up to date or the check fails.
 */
@Composable
fun UpdateGate() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var info by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        info = Updater.check(BuildConfig.UPDATE_MANIFEST_URL, BuildConfig.VERSION_CODE)
    }

    val update = info
    if (update == null || dismissed) return

    AlertDialog(
        onDismissRequest = { if (!downloading) dismissed = true },
        title = { Text("有新版本 ${update.versionName}") },
        text = {
            Column {
                Text(if (update.notes.isNotBlank()) update.notes else "建議更新到最新版本。")
                if (downloading) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("下載中…請稍候")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !downloading,
                onClick = {
                    // Android blocks unknown-source installs until granted once.
                    if (!ApkInstaller.canInstall(context)) {
                        ApkInstaller.requestInstallPermission(context)
                        return@TextButton
                    }
                    downloading = true
                    scope.launch {
                        val apk = Updater.downloadApk(context, update.apkUrl)
                        downloading = false
                        if (apk != null) ApkInstaller.install(context, apk) else dismissed = true
                    }
                },
            ) { Text(if (downloading) "下載中…" else "立即更新") }
        },
        dismissButton = {
            TextButton(enabled = !downloading, onClick = { dismissed = true }) { Text("稍後") }
        },
    )
}
