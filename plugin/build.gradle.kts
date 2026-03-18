import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

plugins {
    alias(libs.plugins.runPaper)
    alias(libs.plugins.gradleShadow)
    alias(libs.plugins.paperLoaderGen)
}

runPaper.folia.registerTask {  }

dependencies {
    // Libs
    compileOnly(libs.paperAPI)
    compileOnly(libs.checkerFramework) // Provided by paper

    paperRuntime(projects.chasmLib)
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        expand(props)
    }

    generatePaperLoader {
        classPath = "dev.hboyd.chasm.ChasmLoader"
    }

    shadowJar {
        archiveClassifier = ""
    }

    jar {
        enabled = false // Only shadowed jar
    }
}