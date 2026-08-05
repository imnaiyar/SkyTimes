import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val moduleNamespace = if (project.path.startsWith(":feature:")) {
    "com.imnaiyar.skytimes.feature.${project.name}"
} else {
    "com.imnaiyar.skytimes.${project.name}"
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        // Only the app module produces the framework consumed by iosApp (baseName "Shared").
        if (project.path == ":app") {
            iosTarget.binaries.framework {
                baseName = "Shared"
                isStatic = true
            }
        }
    }

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = moduleNamespace
        // Keep in sync with gradle/libs.versions.toml (android-compileSdk / android-minSdk).
        compileSdk = 36
        minSdk = 33

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }
}

compose.resources {
    // core owns the shared composeResources; features must be able to use them.
    publicResClass = true
}
