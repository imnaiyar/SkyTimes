plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.reminders.api)
            implementation(projects.features.settings.api)

            // Supertypes of the repositories used by the platform schedulers.
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.onboarding)
        }
    }
}
