val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

plugins {
    idea
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
}

dependencies {
    compileOnly(libs.jspecify)
}

indra {
    javaVersions {
        target(25)
    }

    github("hboyd2003", "chasm") {
        ci(true)
        scm(true)
        publishing(true)
    }

    configurePublications {
        pom {
            developers {
                developer {
                    id = "hboyd2003"
                    name = "Harrison Boyd"
                    email = "8950185+hboyd2003@users.noreply.github.com"
                    timezone = "America/New_York"
                }
            }
        }
    }

    signWithKeyFromPrefixedProperties("hboyd")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}