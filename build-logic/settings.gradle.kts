rootProject.name = "simplefreeze-build-logic"


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
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

    versionCatalogs {
        register("libs") {
            from(files("../gradle/libs.versions.toml")) // include from parent project
        }
    }
}