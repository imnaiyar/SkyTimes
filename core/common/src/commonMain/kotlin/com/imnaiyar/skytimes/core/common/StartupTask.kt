package com.imnaiyar.skytimes.core.common

interface StartupTask {
    val name: String
    val critical: Boolean

    suspend fun initialize()
}
