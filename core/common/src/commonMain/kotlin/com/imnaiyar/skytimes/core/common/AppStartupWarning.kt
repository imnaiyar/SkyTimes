package com.imnaiyar.skytimes.core.common

data class AppStartupWarning(
    val taskName: String,
    val cause: Throwable
)
