plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)

            // image loading + full-screen zoom
            implementation(libs.coil.image)
            implementation(libs.coil.ktor)
            implementation(libs.image.zoom)
            implementation(libs.image.zoom.coil)
        }
    }
}
