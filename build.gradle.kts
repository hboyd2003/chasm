plugins {
    idea
    alias(libs.plugins.gitSimpleSemver)
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
