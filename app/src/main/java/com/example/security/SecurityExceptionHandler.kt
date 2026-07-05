package com.example.security

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

class SecurityExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e("MANNHALKA_SECURITY", "Uncaught Exception Intercepted successfully", throwable)
            
            // Save recovery diagnostics so we can display a self-healing message
            val prefs = context.getSharedPreferences("mannhalka_security_enclave", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("self_healed_flag", true)
                .putString("last_error_message", throwable.message ?: "Unknown thread exception")
                .putString("last_error_trace", Log.getStackTraceString(throwable))
                .apply()
                
            // Attempt to trigger normal recovery loop by restarting to Main Activity
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(context.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            // Ultimate fallback to prevent system crash loop
            defaultHandler?.uncaughtException(thread, throwable)
        } finally {
            exitProcess(2)
        }
    }

    companion object {
        fun initialize(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is SecurityExceptionHandler) {
                Thread.setDefaultUncaughtExceptionHandler(
                    SecurityExceptionHandler(context.applicationContext, defaultHandler)
                )
            }
        }
    }
}
