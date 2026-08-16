plugins {
    alias(conventions.plugins.skytimes.kmp.library)
}

kotlin {
    sourceSets {
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", "2.25.1"))
        }
    }
}
