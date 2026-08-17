val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

plugins {
    idea
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.licenser.spotless")
}

dependencies {
    compileOnly(libs.jspecify)

    testImplementation(libs.bundles.junitJupiterCompile)
    testRuntimeOnly(libs.bundles.junitJupiterRuntime)
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

    lgpl3OrLaterLicense()

    signWithKeyFromPrefixedProperties("hboyd")

    checkstyle(libs.versions.checkstyle.get())
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file(".spotless/license_header_template.txt"))
    newLine(true)
}

spotless {
    java {
        targetExclude("build/generated/**/*.java")
        removeUnusedImports()
        formatAnnotations()
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
