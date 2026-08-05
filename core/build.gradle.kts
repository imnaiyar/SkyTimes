plugins {
    id("skytimes.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // data
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.coroutines)

            // theme
            implementation(libs.material.kolor)

            // media
            implementation(libs.coil.image)
            implementation(libs.coil.ktor)
            implementation(libs.image.zoom)
            implementation(libs.image.zoom.coil)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.1"))
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
