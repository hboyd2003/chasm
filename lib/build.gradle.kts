dependencies {
    implementation(libs.checkerFramework)
    implementation(libs.guava)
    implementation(libs.bundles.adventureAPI)
    implementation(libs.gson)
    implementation(libs.examinationAPI)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
