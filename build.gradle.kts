import dev.hboyd.git_simple_semver.git_semver.textProvider

plugins {
    idea
    alias(libs.plugins.gitSimpleSemver)
    alias(libs.plugins.indraLicenserSpotless).apply(false) // Needed to avoid issue with sibling projects
}

gitSimpleSemver {
    preReleaseIdentifierProviders = listOf(textProvider("SNAPSHOT").onlyIfNotRelease())
    buildIdentifierProviders.add(textProvider("adventure4")) // Always identify build as adventure 4
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
