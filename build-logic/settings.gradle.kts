pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    // Settings plugin: auto-imports the root version catalog into this included build and
    // generates type-safe `libs` accessors for the convention plugins.
    // Version must stay in sync with gradle/libs.versions.toml (typesafeConventions).
    id("dev.panuszewski.typesafe-conventions") version "0.11.1"
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
        gradlePluginPortal()
    }
}

rootProject.name = "build-logic"
include(":convention")
