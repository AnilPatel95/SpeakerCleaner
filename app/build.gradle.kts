import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.shuttletechnologies.speakercleaner"
    compileSdk = 36

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val hasKeystore = keystorePropertiesFile.exists()
    if (hasKeystore) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        create("release") {
            if (hasKeystore) {
                val storeFilePath = keystoreProperties.getProperty("storeFile")
                    ?: keystoreProperties.getProperty("\uFEFFstoreFile")
                if (!storeFilePath.isNullOrBlank()) {
                    val resolvedFile = File(storeFilePath)
                    storeFile = if (resolvedFile.isAbsolute) resolvedFile else rootProject.file(storeFilePath)
                    storePassword = keystoreProperties.getProperty("storePassword")
                    keyAlias = keystoreProperties.getProperty("keyAlias")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                }
            }
        }
    }

    defaultConfig {
        applicationId = "com.shuttletechnologies.speakercleaner"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "1.0.1"

        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/2247696110\"")
        buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            manifestPlaceholders["admobAppId"] = "ca-app-pub-5626817105654582~2313342064"
            buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"ca-app-pub-5626817105654582/2712797313\"")
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-5626817105654582/8778588773\"")
        }
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = true
        shaders = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Architecture Components
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Jetpack Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Biometric Auth
    implementation(libs.androidx.biometric)

    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // AdMob
    implementation(libs.play.services.ads)

    // Firebase BoM, Analytics & Crashlytics
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
