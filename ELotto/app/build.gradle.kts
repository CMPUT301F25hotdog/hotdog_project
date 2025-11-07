plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
}

android {
    namespace = "com.hotdog.elotto"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hotdog.elotto"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArgument("runnerBuilder", "de.mannodermaus.junit5.AndroidJUnit5Builder")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    // --- Firebase (via BOM) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-auth")

    // --- AndroidX + Material ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // ZXing
    implementation("com.google.zxing:core:3.5.3")

    // --- JUnit 5 for Unit Tests (test folder) ---
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")

    // Mockito for JUnit 5
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")

    // Android Test Core
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // --- JUnit 5 for Instrumented Tests (androidTest folder) ---
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")
    androidTestImplementation("org.mockito:mockito-junit-jupiter:5.5.0")

    // AndroidX Test (compatibility)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // For Fragment testing
    androidTestImplementation("androidx.fragment:fragment-testing:1.6.2")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.5")

    // Mockito for Android tests
    androidTestImplementation("org.mockito:mockito-android:5.5.0")

    // Architecture components testing
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
}