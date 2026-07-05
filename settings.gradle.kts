pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("net.fabricmc.fabric-loom") version "1.17.13"
        id("net.neoforged.moddev") version "2.0.141"
    }
}

rootProject.name = "Dynamic Trims"

include("fabric", "neoforge")

project(":fabric").projectDir = file("src/fabric")
project(":neoforge").projectDir = file("src/neoforge")
