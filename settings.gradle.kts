pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net/")
        maven(url = "https://maven.neoforged.net/releases/")
        maven(url = "https://maven.architectury.dev/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("dev.architectury.loom") version "1.10.430" apply false
}

rootProject.name = "Pufferfish's Skills"

include("Common", "Fabric", "NeoForge")
