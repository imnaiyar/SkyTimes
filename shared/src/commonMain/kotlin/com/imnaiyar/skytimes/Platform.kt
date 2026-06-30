package com.imnaiyar.skytimes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform