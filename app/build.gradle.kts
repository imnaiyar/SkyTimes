plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.features.reminders.implementation)
        }
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.ui)
            implementation(projects.core.onboarding)
            implementation(projects.core.navigation)

            implementation(projects.features.quests)
            implementation(projects.features.settings.api)
            implementation(projects.features.settings.implementation)
            implementation(projects.features.reminders.api)
            implementation(projects.features.vault)
            implementation(projects.features.home)

            implementation(libs.compose.navigation3.ui)
            implementation(libs.windows.sizeclass)
        }
        iosMain.dependencies {
            implementation(projects.features.reminders.implementation)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}


dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
