package com.imnaiyar.skytimes.reminder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context

/**
 * Lightweight holder for the application [Context] and optional
 * foreground [Activity] so that platform components (notification
 * manager, alarm scheduler) can obtain them without relying on
 * ViewModels.
 *
 * Initialised once in [com.imnaiyar.skytimes.GameTimeApplication].
 */

@SuppressLint("StaticFieldLeak")
object AndroidContextHolder {
    lateinit var context: Context
        private set

    /** The currently-visible Activity, if any.  May be `null`. */
    var activity: Activity? = null

    /** Callback that bridges [Activity.onRequestPermissionsResult] to a coroutine. */
    var permissionCallback: ((Boolean) -> Unit)? = null

    /** Request code used by [ActivityCompat.requestPermissions]. */
    const val PERMISSION_REQUEST_CODE = 1001

    fun initialize(appContext: Context) {
        if (!::context.isInitialized) {
            context = appContext.applicationContext
        }
    }
}
