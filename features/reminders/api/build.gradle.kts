plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
