import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.cadence.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cadence.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 12
        versionName = "0.9.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

// Poppins is bundled at build time so the app stays 100% offline.
val fontMirrors = listOf(
    "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/poppins/",
    "https://raw.githubusercontent.com/google/fonts/main/ofl/poppins/",
    "https://github.com/google/fonts/raw/main/ofl/poppins/",
)

val downloadFonts = tasks.register("downloadFonts") {
    onlyIf { !file("src/main/res/font/poppins_regular.ttf").exists() }
    doLast {
        val dir = file("src/main/res/font").apply { mkdirs() }
        mapOf(
            "poppins_regular.ttf" to "Poppins-Regular.ttf",
            "poppins_medium.ttf" to "Poppins-Medium.ttf",
            "poppins_semibold.ttf" to "Poppins-SemiBold.ttf",
            "poppins_bold.ttf" to "Poppins-Bold.ttf",
        ).forEach { (name, remote) ->
            val target = File(dir, name)
            if (target.exists()) return@forEach
            var lastError: Exception? = null
            for (mirror in fontMirrors) {
                try {
                    URL(mirror + remote).openConnection().apply {
                        connectTimeout = 20000
                        readTimeout = 30000
                    }.getInputStream().use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    lastError = null
                    break
                } catch (e: Exception) {
                    lastError = e
                }
            }
            lastError?.let { throw it }
        }
    }
}
tasks.named("preBuild") { dependsOn(downloadFonts) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
