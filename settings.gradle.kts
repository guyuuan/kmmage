pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    
}
rootProject.name = "kmmage"


include(":android")
include(":desktop")
include(":common")

enableFeaturePreview("VERSION_CATALOGS")