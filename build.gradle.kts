plugins {
    `maven-publish`
    idea
    alias(libs.plugins.gitSimpleSemver)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}