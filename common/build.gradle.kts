import org.jetbrains.compose.compose
@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id(libs.plugins.jetbarins.compose.get().pluginId)
    kotlin("plugin.serialization")
}

kotlin {
    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                api(compose.material)
                implementation(libs.ktor.core)
                implementation(libs.okio)
                implementation(libs.coroutines.core)
                implementation("androidx.annotation:annotation:1.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api(libs.androidx.appcompat)
                api(libs.androidx.core)
                implementation(libs.ktor.android)
                implementation(libs.ktor.cio)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.core)
                implementation(libs.ktor.cio)
                implementation(libs.thumbnailator)
                implementation(libs.coroutines.swing)
            }
        }
        val desktopTest by getting
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}