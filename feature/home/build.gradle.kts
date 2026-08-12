plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(projects.feature.quests)
            implementation(projects.feature.reminders)
            implementation(projects.feature.settings)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(libs.compose.navigationevent)
            implementation(libs.compose.navigation3.ui)
            implementation(libs.reorderable)
            implementation(libs.material.kolor)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(libs.kotlinx.datetime)
        }
    }
}
