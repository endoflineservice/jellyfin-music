plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import java.util.Properties

val releaseKeystorePropertiesFile = rootProject.file("keystore.properties")
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystorePropertiesFile.isFile) {
        releaseKeystorePropertiesFile.inputStream().use(::load)
    }
}

fun releaseSigningValue(name: String): String? =
    releaseKeystoreProperties.getProperty(name)
        ?: providers.gradleProperty(name).orNull
        ?: System.getenv(name)

val releaseStoreFilePath = releaseSigningValue("JELLYFIN_MUSIC_UPLOAD_STORE_FILE")
val releaseStorePassword = releaseSigningValue("JELLYFIN_MUSIC_UPLOAD_STORE_PASSWORD")
val releaseKeyAlias = releaseSigningValue("JELLYFIN_MUSIC_UPLOAD_KEY_ALIAS")
val releaseKeyPassword = releaseSigningValue("JELLYFIN_MUSIC_UPLOAD_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "dev.cholt.jellyfinmusic"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.cholt.jellyfinmusic"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "1.1.7"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            isShrinkResources = false
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.foundation:foundation:1.8.3")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.media3:media3-datasource:1.10.1")
    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("com.android.billingclient:billing:9.1.0")
    implementation("ir.mahozad.multiplatform:wavy-slider:2.2.0")
}

tasks.register("printPlayReleaseSigningStatus") {
    doLast {
        if (hasReleaseSigning) {
            println("Play release signing is configured for $releaseStoreFilePath")
        } else {
            println("Play release signing is not configured. Release APK/AAB outputs will be unsigned until keystore.properties or matching environment variables are provided.")
        }
    }
}

tasks.register("verifyPlayReleaseSigning") {
    doLast {
        check(hasReleaseSigning) {
            "Play release signing is not configured. Copy keystore.properties.example to keystore.properties or provide the JELLYFIN_MUSIC_UPLOAD_* environment variables."
        }
        check(file(releaseStoreFilePath!!).isFile) {
            "Play release keystore was configured but not found: $releaseStoreFilePath"
        }
        println("Play release signing is ready for $releaseStoreFilePath")
    }
}
