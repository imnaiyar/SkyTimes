package com.imnaiyar.skytimes

import android.app.Application
import com.imnaiyar.skytimes.reminder.AndroidContextHolder

/**
 * Custom [Application] subclass.
 *
 * Responsibilities:
 * - Initialises [AndroidContextHolder] so that platform reminder
 *   components can access the application context without coupling
 *   to Activities or ViewModels.
 */
class GameTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.initialize(this)
    }
}
