package com.example.security

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoHelper {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    /**
     * Derives a 256-bit AES key from a string (like a chatId) using SHA-256.
     */
    private fun deriveKey(seed: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(seed.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts a message using AES-CBC with a key derived from the given seed (chatId).
     * Returns a Pair of (Base64 Encrypted String, Base64 IV String)
     */
    fun encrypt(message: String, seed: String): Pair<String, String> {
        return try {
            val keySpec = deriveKey(seed)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            
            val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
            val ivBytes = cipher.iv
            
            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(ivBytes, Base64.NO_WRAP)
            
            Pair(encryptedBase64, ivBase64)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(message, "") // fallback
        }
    }

    /**
     * Decrypts a message using AES-CBC with a key derived from the given seed (chatId) and the provided IV.
     */
    fun decrypt(encryptedMessage: String, ivBase64: String, seed: String): String {
        if (ivBase64.isEmpty()) return encryptedMessage
        return try {
            val keySpec = deriveKey(seed)
            val cipher = Cipher.getInstance(ALGORITHM)
            val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
            val ivSpec = IvParameterSpec(ivBytes)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedMessage, Base64.NO_WRAP))
            
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "Decryption Error: [Integrity Compromised]"
        }
    }
}
