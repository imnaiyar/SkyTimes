package com.imnaiyar.skytimes.startup

interface StartupTask {
    val name: String
    val critical: Boolean

    suspend fun run()
}
