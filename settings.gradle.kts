pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://maven.architectury.dev/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.architectury.loom") version "1.3.358" apply false
}

rootProject.name = "Pufferfish's Skills"

include("Common", "Fabric", "Forge")
