plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "se.ltu.navigator"
    compileSdk = 35

    defaultConfig {
        applicationId = "se.ltu.navigator"
        minSdk = 31
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.google.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.commons.io)
    implementation(libs.wear.remote.interactions)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.mapsforge.map.android.v0230)
    implementation(libs.mapsforge.map.v0230)
    implementation(libs.mapsforge.map.reader.v0230)
    implementation(libs.mapsforge.core.v0230)
    implementation(libs.play.services.wearable)
    implementation(libs.gson)
}