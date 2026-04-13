val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

plugins {
    java
    `maven-publish`
    idea
}

dependencies {
    compileOnly(libs.jspecify)
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
                username = System.getenv("HBOYD_DEV_REPO_USERNAME")
                password = System.getenv("HBOYD_DEV_REPO_PASSWORD")
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