plugins {
    id("net.neoforged.moddev")
}

neoForge {
    version = rootProject.property("neoforge_loader").toString()

    runs {
        create("client") {
            client()
            gameDirectory = rootProject.file("run/neoforge")
        }

        create("server") {
            server()
            gameDirectory = rootProject.file("run/neoforge")
            programArgument("nogui")
        }
    }

    mods {
        create("dynamictrim") {
            sourceSet(sourceSets.main.get())
        }
    }
}
