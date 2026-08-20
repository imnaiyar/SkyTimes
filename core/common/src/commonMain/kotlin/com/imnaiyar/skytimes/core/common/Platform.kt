package com.imnaiyar.skytimes.core.common

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform