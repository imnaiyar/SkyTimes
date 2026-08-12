plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.feature.home)
            implementation(projects.feature.quests)
            implementation(projects.feature.reminders)
            implementation(projects.feature.settings)
            implementation(projects.feature.vault)

            // navigation
            implementation(libs.compose.navigationevent)
            implementation(libs.compose.navigation3.ui)

            // platform
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // ui
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiToolingPreview)

            // theme
            implementation(libs.material.kolor)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
