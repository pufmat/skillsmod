pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://maven.architectury.dev/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Pufferfish's Skills"

include("Common", "Fabric", "Forge")
