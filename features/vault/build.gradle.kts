plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.ui)
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.common)
            implementation(projects.core.navigation)
            implementation(libs.compose.navigation3.ui)
        }
    }
}
