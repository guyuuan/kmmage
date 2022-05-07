plugins {
     id(libs.plugins.jetbarins.compose.get().pluginId)
    id("com.android.application")
    kotlin("android")
}

group = "cn.chitanda"
version = "0.0-alpha"


dependencies {
    implementation(project(":common"))
    implementation(libs.ktor.android)
    implementation("androidx.activity:activity-compose:1.4.0")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "cn.chitanda.kmmage.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}