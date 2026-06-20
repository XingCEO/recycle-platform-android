plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.recycle.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
        // Defaults; apps override at runtime via ServerConfig (SharedPreferences).
        // Cloud Supabase (project slnupbmwrzaycnmizmme); for local dev enter
        // http://10.0.2.2:54321 + local anon key on the login screen.
        buildConfigField("String", "DEFAULT_SUPABASE_URL", "\"https://slnupbmwrzaycnmizmme.supabase.co\"")
        buildConfigField(
            "String", "DEFAULT_SUPABASE_ANON_KEY",
            "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNsbnVwYm13cnpheWNubWl6bW1lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODExOTA0MjYsImV4cCI6MjA5Njc2NjQyNn0.9-s7WrN3ht2en3fT-9dDlEaAAj6jkm6jUmgFqxjOcAw\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.zxing.core)
    testImplementation(libs.junit)
}
