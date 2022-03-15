plugins {
    `kotlin-dsl`
}
sourceSets{
    val main by getting {
        java.srcDirs+=file("src/main/kotlin")
    }
}

repositories {
    gradlePluginPortal()
}