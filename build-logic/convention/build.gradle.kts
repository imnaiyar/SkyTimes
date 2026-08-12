plugins {
    `kotlin-dsl`
}

// No plugin-marker dependencies needed here: the typesafe-conventions settings plugin
// adds them automatically (with versions from gradle/libs.versions.toml) for every
// `alias(libs.plugins.*)` used in the convention plugin scripts.
