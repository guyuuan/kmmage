plugins{
    `kotlin-dsl`
}
repositories {
    gradlePluginPortal()
}

sourceSets {
    val main by getting {
        java.srcDirs.add(File("src/main/kotlin"))
    }
}