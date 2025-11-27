plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("de.mannodermaus.android-junit5") version "1.14.0.0"
}

android {
    namespace = "com.hotdog.elotto"
    compileSdk = 36

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }

    defaultConfig {
        applicationId = "com.hotdog.elotto"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }

    tasks.withType<Test>{
        useJUnitPlatform()
    }
}

dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)

    // Core Android
    implementation(libs.core)
    implementation(libs.recyclerview)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)



    // Lifecycle
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // ========================================
    // ADMIN SCREENS DEPENDENCIES - ADD THESE
    // ========================================

    // Glide for image loading (REQUIRED for admin screens)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // CardView (for admin UI cards)
    implementation("androidx.cardview:cardview:1.0.0")

    // ========================================

    // ZXing for QR codes
    implementation(libs.zxing.core)

    // Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockito.junit)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.android.test.runner)
    testRuntimeOnly(libs.junit.engine)

    // Android Testing
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.junit.jupiter)
    androidTestRuntimeOnly(libs.android.test.runner)
    androidTestImplementation(libs.core.v161)
    androidTestImplementation(libs.runner)
}

tasks.withType<Test>{
    useJUnitPlatform()
}