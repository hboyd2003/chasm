plugins {
    id("chasm.java-conventions")
    `java-library`
}

dependencies {
    implementation(libs.guava)
    implementation(libs.gson)

    api(libs.checkerFramework)
    api(libs.jspecify)
    api(libs.bundles.adventureAPI)
    api(libs.examinationAPI)
}

indra {
    publishReleasesTo("hboydDev", "https://repo.hboyd.dev/releases")
    publishSnapshotsTo("hboydDev", "https://repo.hboyd.dev/snapshots")
}