plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.com.google.dagger.hilt)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.google.experiment.soundexplorer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.google.experiment.soundexplorer"
        minSdk = 34
        //noinspection OldTargetApi
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // XR
    implementation(libs.androidx.xr.compose)
    implementation(libs.androidx.xr.runtime)
    implementation(libs.androidx.xr.scenecore)
    implementation(libs.androidx.xr.arcore)
    implementation(libs.androidx.xr.material3)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3.android)
    implementation(libs.androidx.compose.ui.tooling.preview)
//    // Optional - Included automatically by material, only add when you need
//    // the icons but not the material library (e.g. when using Material3 or a
//    // custom design system based on Foundation)
//    implementation("androidx.compose.material:material-icons-core")
//    // Optional - Add full set of material icons
//    implementation("androidx.compose.material:material-icons-extended")

    // material icons
    implementation(libs.androidx.compose.material.icons.core)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}