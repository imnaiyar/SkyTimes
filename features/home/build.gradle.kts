plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.onboarding)

            implementation(projects.features.quests)
            implementation(projects.features.settings.api)
            implementation(projects.features.reminders.api)

            implementation(libs.reorderable)
            implementation(libs.compose.navigationevent)
            implementation(libs.compose.navigation3.ui)
        }
    }
}
