rootProject.name = "SkyTimes"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":app")
include(":webApp")
include(":core:common")
include(":core:domain")
include(":core:ui")
include(":core:onboarding")
include(":core:navigation")
include(":core:data")
include(":features:quests")
include(":features:settings:api")
include(":features:settings:implementation")
include(":features:reminders:api")
include(":features:reminders:implementation")
include(":features:vault")
include(":features:home")