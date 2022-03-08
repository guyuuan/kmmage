buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin_version}")
        classpath("com.android.tools.build:gradle:4.1.3")
    }
}
plugins{
    kotlin("multiplatform") version Versions.kotlin_version apply  false
    id("org.jetbrains.compose") version  Versions.compose_version apply false
}
group = "me.chunjinchen"
version = "1.0"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}