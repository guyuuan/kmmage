buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradleplugin.kotlin)
        classpath(libs.gradleplugin.android)
    }
}

plugins {
//    kotlin("multiplatform") version libs.versions.kotlin apply false
    kotlin("multiplatform") version libs.versions.kotlin apply  false
    alias(libs.plugins.jetbarins.compose) apply  false
    kotlin("plugin.serialization") version libs.versions.kotlin apply  false
}

allprojects {
    group = "cn.chitanda"
    version = "0.0-alpha"
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}