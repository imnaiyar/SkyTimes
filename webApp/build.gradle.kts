import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)

            implementation(libs.compose.ui)
        }
    }
}

val syncWebTitle by tasks.registering {
    description = "Syncs app configuration like name  with index.html"

    val appName = project.findProperty("app.name").toString()
    val resourcesDir = file("src/webMain/resources")
    val indexHtml = resourcesDir.resolve("index.html")

    doLast {
        if (indexHtml.exists()) {
            val content = indexHtml.readText()
            val newContent = content.replace(Regex("<title>.*</title>"), "<title>$appName</title>")
            indexHtml.writeText(newContent)
        }
    }
}

tasks.named("jsProcessResources") {
    dependsOn(syncWebTitle)
}

tasks.named("wasmJsProcessResources") {
    dependsOn(syncWebTitle)
}
