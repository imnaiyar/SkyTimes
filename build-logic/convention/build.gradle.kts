plugins {
    `kotlin-dsl`
}

// Plugin marker coordinates. Versions must stay in sync with gradle/libs.versions.toml.
dependencies {
    implementation("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:2.4.0")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.4.0")
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:2.4.0")
    implementation("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:1.11.1")
    implementation("com.android.kotlin.multiplatform.library:com.android.kotlin.multiplatform.library.gradle.plugin:9.0.1")
}
