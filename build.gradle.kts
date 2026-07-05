@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

val minecraftVersion = property("minecraft_version").toString()
val minecraftLine = property("minecraft_line").toString()
val javaVersion = property("java_version").toString().toInt()
val buildNumber = property("build_number").toString()
val modArtifactName = property("mod_artifact_name").toString()
val modDescription = property("mod_description").toString()
val modMetadataVersion = "$minecraftLine-build.$buildNumber"

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

    apply(plugin = "java")

    version = "$minecraftLine-$loader-build-$buildNumber"
    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(loader))
    extensions.configure<BasePluginExtension> {
        archivesName.set(modArtifactName)
    }

    extensions.configure<SourceSetContainer> {
        named("main") {
            java.setSrcDirs(listOf(rootProject.file("src/common/java"), rootProject.file("src/$loader/java")))
            resources.setSrcDirs(listOf(rootProject.file("src/common/resources"), rootProject.file("src/$loader/resources")))
        }
    }

    tasks {
        withType<JavaCompile> {
            options.release.set(javaVersion)
        }

        named<ProcessResources>("processResources") {
            val modMetadata = mapOf(
                "description" to modDescription,
                "version" to modMetadataVersion,
                "minecraft_dependency" to rootProject.property("${loader}_minecraft_dependency").toString(),
                "minecraft_version" to minecraftVersion,
                "minecraft_line" to minecraftLine,
                "java_version" to javaVersion.toString(),
                "loader_version" to loaderMetadataVersion
            )

            inputs.properties(modMetadata)
            filesMatching("fabric.mod.json") { expand(modMetadata) }
            filesMatching("META-INF/neoforge.mods.toml") { expand(modMetadata) }
        }

        withType<AbstractCopyTask> {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        named<Jar>("jar") {
            archiveBaseName.set(modArtifactName)
        }

        named<Delete>("clean") {
            delete(layout.buildDirectory)
        }

        register<Copy>("buildAndCollect") {
            group = "build"
            from(named<Jar>("jar").flatMap { it.archiveFile })
            into(rootProject.layout.buildDirectory.dir("libs/$modMetadataVersion"))
            dependsOn("build")
        }
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()

        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
}
