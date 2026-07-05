package com.example.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * BiometricBridge is a React-compatible biometric authentication wrapper.
 * It exposes Promise-based methods mimicking a React Native native module bridge,
 * allowing FaceID/Fingerprint integration for local data protection.
 */
object BiometricBridge {

    /**
     * Interface mimicking the standard React Native Promise interface.
     * This makes it trivial to map or export these native endpoints to
     * a React Native or Cordova / Web environment in the future.
     */
    interface BiometricPromise {
        fun resolve(value: Any?)
        fun reject(code: String, message: String)
    }

    /**
     * Check if biometric features (Fingerprint / FaceID) are available and enrolled on the device.
     * Maps directly to React-compatible bridge signature.
     */
    fun canAuthenticate(context: Context, promise: BiometricPromise) {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                promise.resolve(true)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                promise.reject("BIOMETRIC_ERROR_NO_HARDWARE", "No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                promise.reject("BIOMETRIC_ERROR_HW_UNAVAILABLE", "Biometric features are currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                promise.reject("BIOMETRIC_ERROR_NONE_ENROLLED", "No biometric credentials enrolled. Please set up a fingerprint or face unlock in your device settings.")
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                promise.reject("BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED", "A security update is required before biometrics can be used.")
            }
            else -> {
                promise.reject("BIOMETRIC_ERROR_UNKNOWN", "Unknown biometric hardware or enrollment error.")
            }
        }
    }

    /**
     * Launch the native OS biometric prompt.
     * This authenticates the user using secure local device authentication.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        promise: BiometricPromise
    ) {
        activity.runOnUiThread {
            try {
                val executor: Executor = ContextCompat.getMainExecutor(activity)
                
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // User cancelled or authentication failed too many times
                        promise.reject("AUTH_ERROR_$errorCode", errString.toString())
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        promise.resolve("SUCCESS")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Biometric scanned but not recognized. The OS dialog will display
                        // "Not recognized. Try again." and handle retries internally.
                    }
                }

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .setConfirmationRequired(false)
                    .build()

                val biometricPrompt = BiometricPrompt(activity, executor, callback)
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                promise.reject("BRIDGE_EXCEPTION", e.message ?: "An unexpected exception occurred in BiometricBridge.")
            }
        }
    }
}
