import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

/**
 * Android namespace for this module, also used to derive the generated
 * Compose-resources package. Derived from the Gradle project path:
 *
 *   :core:ui                        -> com.imnaiyar.skytimes.core.ui
 *   :features:settings:implementation -> com.imnaiyar.skytimes.feature.settings.implementation
 *   :app                            -> com.imnaiyar.skytimes.app
 *
 * Override via `skytimes.namespace` in the module's gradle.properties.
 */
val moduleNamespace: String = project.findProperty("skytimes.namespace") as String?
    ?: buildString {
        append("com.imnaiyar.skytimes")
        project.path
            .removePrefix(":")
            .split(":")
            .filter { it.isNotBlank() }
            .map { if (it == "features") "feature" else it }
            .forEach { append('.').append(it) }
    }

compose.resources {
    packageOfResClass = "$moduleNamespace.generated.resources"
    publicResClass = true
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = moduleNamespace
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            // serialization + platform primitives
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.coroutines)

            // lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // theme color generation
            implementation(libs.material.kolor)
        }
    }
}
