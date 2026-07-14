plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

tasks.register("syncIosConfig") {
    group = "ios"
    description = "Synchronizes app configuration to iosApp/Configuration/Config.xcconfig"

    val appId = project.findProperty("app.id")
    val appName = project.findProperty("app.name")
    val configFile = file("iosApp/Configuration/Config.xcconfig")

    doLast {
        val content = configFile.readLines().joinToString("\n") { line ->
            when {
                line.startsWith("APP_ID=") -> "APP_ID=$appId"
                line.startsWith("APP_NAME=") -> "APP_NAME=$appName"
                else -> line
            }
        }
        configFile.writeText(content + "\n")
    }
}