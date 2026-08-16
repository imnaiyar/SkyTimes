plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.onboarding)
            implementation(libs.compose.navigation3.ui)
        }
    }
}
