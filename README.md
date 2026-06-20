# ♻️ 回收獎勵平台 — Android

回收獎勵平台的兩支 Android App（使用者 / 廠商）與共用核心模組，後端為 Supabase。
Jetpack Compose + Material 3，核心為手寫的 OkHttp / PostgREST 客戶端。

---

## 📥 下載安裝

> APK 不在程式碼樹（編譯產物不進 git），而是發佈在 **[Releases](../../releases)**。

| App | 下載（永遠最新版） |
| --- | --- |
| 👤 使用者 App | **[user-app-debug.apk](../../releases/latest/download/user-app-debug.apk)** |
| 🏭 廠商 App | **[vendor-app-debug.apk](../../releases/latest/download/vendor-app-debug.apk)** |

也可到 [Releases 頁面](../../releases/latest) 手動下載。

### 安裝步驟（手機）

1. 用手機瀏覽器點上面的下載連結。
2. 第一次安裝會跳「不允許安裝未知來源」→ 點 **設定** → 開啟「允許此來源」。
3. 安裝完成，開啟即可（測試帳號見下）。

> ⚠️ **覆蓋安裝**：若手機已裝過舊版，且兩者**簽章相同**（同一台電腦 build 的 debug 版）可直接覆蓋、資料保留。若出現「應用程式未安裝／簽章不符」，請先**解除安裝舊版**再裝（本機資料清掉，重新登入即可）。

### 測試帳號（密碼皆 `password123`）

| 帳號 | 角色 |
| --- | --- |
| `user1@demo.test` | 使用者 |
| `vendor1@demo.test` | 廠商 |

---

## 🔄 App 內自動更新

兩支 App 內建自更（無 Play Store / OTA）：啟動時讀取本 repo 的
[`update/*.json`](update/) 清單，若版本較新即跳出提示 → 下載 → 安裝。
網路失敗時靜默略過，不打擾使用者。

> ⚠️ 自動更新從 **v1.1（versionCode 2）起**才有。更早的舊版沒有此功能，
> 朋友需先**手動安裝一次** v1.1，之後即可自動更新。

---

## ✨ 功能

- **使用者 App**：個人 QR 身分碼（5 分鐘輪換）、回收紀錄、點數商店、兌換紀錄。
- **廠商 App**：掃描使用者 QR、條碼回收、雲端 AI 影像辨識回收物、自動發點。
- **共用核心（`core`）**：登入 / Session（含 JWT 過期自動換新、失效自動回登入頁）、
  PostgREST 讀取、RPC 發點、QR 編解碼、自動更新。

---

## 🛠 建置

無全域 Gradle；用 wrapper 或專案內建發行版。

```powershell
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
# 用 wrapper：
./gradlew :user-app:assembleDebug :vendor-app:assembleDebug
./gradlew :core:testDebugUnitTest        # 核心單元測試
# APK 產出 -> {user-app,vendor-app}/build/outputs/apk/debug/
```

版本鎖定：Gradle **8.11.1** ↔ AGP **8.9.2** ↔ Kotlin **2.1.0**，`compileSdk=36`、`minSdk=26`。

---

## 🚀 發佈新版本

1. 在 `user-app/build.gradle.kts` 與 `vendor-app/build.gradle.kts` 把 `versionCode` +1、更新 `versionName`。
2. 重新 build：`./gradlew :user-app:assembleDebug :vendor-app:assembleDebug`。
3. 建立 GitHub Release 並掛上兩個 APK（檔名需維持 `user-app-debug.apk`、`vendor-app-debug.apk`）：
   ```bash
   gh release create v1.2 \
     user-app/build/outputs/apk/debug/user-app-debug.apk \
     vendor-app/build/outputs/apk/debug/vendor-app-debug.apk \
     --title "v1.2" --notes "更新內容…"
   ```
4. 更新 [`update/user.json`](update/user.json) 與 [`update/vendor.json`](update/vendor.json) 的
   `versionCode`、`versionName`、`apkUrl`、`notes`，commit + push。
5. 既有使用者下次開啟 App 即會收到更新提示。

---

> 示範等級專案（demo grade）。後端為 Supabase（Postgres + GoTrue + PostgREST + RLS）；
> App 內僅含 anon 公開金鑰（RLS 保護，設計上即隨 App 出貨）。
