@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.compile.JavaCompile
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
    java
    id("dev.architectury.loom") version "1.9.428" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
}

val minecraftVersion = property("minecraft_version").toString()
val javaVersion = 21
val buildNumber = property("build_number").toString()
val modArtifactName = property("mod_artifact_name").toString()
val modDescription = property("mod_description").toString()
val modMetadataVersion = "$minecraftVersion-build.$buildNumber"

allprojects {
    group = property("mod_group").toString()
    version = modMetadataVersion

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

subprojects {
    val loader = name
    val loaderVersion = property("${loader}_loader").toString()
    val loaderMetadataVersion = findProperty("${loader}_loader_dependency")?.toString() ?: loaderVersion
    val awName = "$minecraftVersion.accesswidener"

    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(loader))
    extra["loom.platform"] = loader

    apply(plugin = "java")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")

    version = "$minecraftVersion-$loader-build-$buildNumber"
    base.archivesName.set(modArtifactName)

    sourceSets {
        main {
            java.setSrcDirs(
                listOf(
                    rootProject.file("src/common/java"),
                    rootProject.file("src/$loader/java")
                )
            )
            resources.setSrcDirs(
                listOf(
                    rootProject.file("src/common/resources"),
                    rootProject.file("src/$loader/resources")
                )
            )
        }
    }

    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
    }

    extensions.configure<LoomGradleExtensionAPI> {
        accessWidenerPath.set(rootProject.file("src/common/resources/$awName"))

        runConfigs.all {
            ideConfigGenerated(true)
            runDir = "../../run/$loader"
        }

        runConfigs["server"].apply {
            programArgs("nogui")
        }
    }

    dependencies {
        if (loader == "fabric") {
            "mappings"(project.extensions.getByType<LoomGradleExtensionAPI>().officialMojangMappings())
            "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")
        }

        if (loader == "neoforge") {
            "mappings"(project.extensions.getByType<LoomGradleExtensionAPI>().officialMojangMappings())
            "neoForge"("net.neoforged:neoforge:$loaderVersion")
            "forgeRuntimeLibrary"("cpw.mods:modlauncher:11.0.5") {
                exclude(group = "cpw.mods", module = "securejarhandler")
            }
        }
    }

    tasks {
        withType<JavaCompile> {
            options.release.set(javaVersion)
        }

        processResources {
            val modMetadata = mapOf(
                "description" to modDescription,
                "version" to modMetadataVersion,
                "minecraft_dependency" to rootProject.property("${loader}_minecraft_dependency").toString(),
                "minecraft_version" to minecraftVersion,
                "loader_version" to loaderMetadataVersion
            )

            inputs.properties(modMetadata)
            filesMatching("fabric.mod.json") { expand(modMetadata) }
            filesMatching("META-INF/neoforge.mods.toml") { expand(modMetadata) }
        }

        withType<AbstractCopyTask> {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        clean {
            delete(layout.buildDirectory)
        }

        if (loader == "neoforge") {
            named<RemapJarTask>("remapJar") {
                atAccessWideners.add(awName)
            }
        }

        register<Copy>("buildAndCollect") {
            group = "build"
            from(named<RemapJarTask>("remapJar").flatMap { it.archiveFile })
            into(rootProject.layout.buildDirectory.file("libs/$modMetadataVersion"))
            dependsOn("build")
        }
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }

}
