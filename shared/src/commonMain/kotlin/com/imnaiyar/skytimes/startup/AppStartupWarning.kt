package com.imnaiyar.skytimes.startup

data class AppStartupWarning(
    val taskName: String,
    val cause: Throwable
)
