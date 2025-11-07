plugins {
    alias(libs.plugins.android.application)
    // Use the version-catalog alias for consistency
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.hotdog.elotto"
    compileSdk = 36   // If you don't have SDK 36 installed, change to 34 or 35.

    defaultConfig {
        applicationId = "com.hotdog.elotto"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ⚠️ If you hit toolchain errors with Java 18, switch both to VERSION_17.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Firebase (via BOM from your version catalog)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

    // JUnit (from your version catalog)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)

    // AndroidX + Material
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // UI + tools you’re already using
    implementation("com.google.zxing:core:3.5.3")

    // Instrumented tests
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
