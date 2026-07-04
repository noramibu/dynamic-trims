pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Dynamic Trims"

include("fabric", "neoforge")

project(":fabric").projectDir = file("src/fabric")
project(":neoforge").projectDir = file("src/neoforge")
