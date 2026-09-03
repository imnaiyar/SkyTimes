plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.ui)
            implementation(projects.core.data)
            implementation(projects.core.common)
        }
    }
}
