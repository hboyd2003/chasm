plugins {
    id("java")
    id("maven-publish")
    id("idea")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "hboyd-dev-repo"
        url = uri("https://repo.hboyd.dev/snapshots/")
    }
    maven {
        name = "codemc-releases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

subprojects {
    plugins.apply("java")
    plugins.apply("maven-publish")
    plugins.apply("idea")

    group = rootProject.group
    version = rootProject.version

    repositories.addAll(rootProject.repositories)

    dependencies {
        compileOnly(rootProject.libs.jspecify)
    }

    val targetJavaVersion = 25
    java {
        withJavadocJar()
        withSourcesJar()

        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
        }
    }

    publishing {
        repositories {
            maven {
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }

                name = "hboyd-dev-repo"
                url = uri("https://repo.hboyd.dev/" + (if (version.toString().contains("SNAPSHOT")) "snapshots/" else "releases/"))
            }
        }
    }

    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }

    tasks.withType(Javadoc::class).configureEach {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all,-missing")
    }
}

// Prevent root project from generating empty jars
tasks {
    test {
        enabled = false
    }
    assemble {
        enabled = false
    }
    jar {
        enabled = false
    }
}

