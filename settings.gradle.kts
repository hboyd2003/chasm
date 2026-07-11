enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "chasm"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.papermc.io/repository/maven-snapshots/") {
            name = "papermc-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        maven(url = "https://repo.hboyd.dev/releases/") {
            name = "hboyd-dev-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.hboyd.dev/snapshots/") {
            name = "hboyd-dev-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        maven(url = "https://repo.codemc.io/repository/maven-releases/") {
            name = "codemc-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.codemc.io/repository/maven-snapshots/") {
            name = "codemc-snapshots"
            mavenContent { snapshotsOnly() }
        }
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.papermc.io/repository/maven-snapshots/") {
            name = "papermc-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        maven(url = "https://repo.hboyd.dev/releases/") {
            name = "hboyd-dev-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.hboyd.dev/snapshots/") {
            name = "hboyd-dev-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        gradlePluginPortal()
    }
}


sequenceOf(
        "lib",
        "plugin"
).forEach {
    include("chasm-$it")
    project(":chasm-$it").projectDir = file(it)
}