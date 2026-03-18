enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "chasm"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/") {
            name = "papermc"
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