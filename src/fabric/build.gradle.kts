plugins {
    id("net.fabricmc.fabric-loom")
}

val minecraftVersion = rootProject.property("minecraft_version").toString()
val fabricLoader = rootProject.property("fabric_loader").toString()

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoader")
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run/fabric"
    }

    runConfigs["server"].apply {
        programArgs("nogui")
    }
}
