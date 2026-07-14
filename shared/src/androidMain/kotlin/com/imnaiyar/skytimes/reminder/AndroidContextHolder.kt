package com.imnaiyar.skytimes.reminder

import android.content.Context

/**
 * Lightweight holder for the application [Context] so that platform
 * components (notification manager, alarm scheduler) can obtain it
 * without relying on Activities or ViewModels.
 *
 * Initialised once in [com.imnaiyar.skytimes.GameTimeApplication].
 */
object AndroidContextHolder {
    lateinit var context: Context
        private set

    fun initialize(appContext: Context) {
        if (!::context.isInitialized) {
            context = appContext.applicationContext
        }
    }
}
