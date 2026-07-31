package com.imnaiyar.skytimes.reminders

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object ContextHolder {
    lateinit var context: Context
        private set

    fun initialize(context: Context) {
        this.context = context
    }
}