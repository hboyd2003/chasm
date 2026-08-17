plugins {
    idea
    alias(libs.plugins.gitSimpleSemver)
    alias(libs.plugins.indraLicenserSpotless).apply(false) // Needed to avoid issue with sibling projects
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks {
    jar {
        enabled = false
    }
}
