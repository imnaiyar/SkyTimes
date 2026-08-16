plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.onboarding)

            implementation(projects.features.reminders.api)
        }
    }
}
