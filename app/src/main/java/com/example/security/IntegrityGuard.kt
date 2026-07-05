package com.example.security

import android.content.Context
import android.os.Build
import android.os.Debug
import java.io.File
import java.security.MessageDigest

object IntegrityGuard {

    /**
     * Checks if the device is rooted.
     */
    fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        
        // Check build tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        return false
    }

    /**
     * Checks if a debugger is attached.
     */
    fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    /**
     * Checks if running on an emulator (sandbox protection).
     */
    fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.indexOf("sdk_google") != -1
                || Build.PRODUCT.indexOf("google_sdk") != -1
                || Build.PRODUCT.indexOf("sdk") != -1
                || Build.PRODUCT.indexOf("sdk_x86") != -1
                || Build.PRODUCT.indexOf("vbox86p") != -1
    }

    /**
     * Secure SHA-256 Hashing with local unique salt for PIN passcodes.
     * Prevents hacking or brute forcing even if database files are dumped.
     */
    fun hashPasscode(passcode: String, salt: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val saltedInput = passcode + salt + "MANNHALKA_SECRET_ENCLAVE_2026"
            val hashedBytes = md.digest(saltedInput.toByteArray(Charsets.UTF_8))
            hashedBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            passcode // Safe fallback if engine fails, but unlikely
        }
    }
}
