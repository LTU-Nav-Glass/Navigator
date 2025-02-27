plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "se.ltu.wearnavigator"
    compileSdk = 35

    defaultConfig {
        applicationId = "se.ltu.wearnavigator"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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

    implementation(libs.play.services.wearable)
    implementation(libs.appcompat)
    implementation(libs.google.material)
    implementation(libs.mapsforge.map.android.v0230)
    implementation(libs.mapsforge.map.v0230)
    implementation(libs.mapsforge.map.reader.v0230)
    implementation(libs.mapsforge.core.v0230)
    implementation(libs.gson)
}