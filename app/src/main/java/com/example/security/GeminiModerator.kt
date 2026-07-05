package com.example.security

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiModerator {
    private const val TAG = "GeminiModerator"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    data class ModerationResult(
        val isSafe: Boolean,
        val flagReason: String
    )

    suspend fun moderateContent(text: String): ModerationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.w(TAG, "Gemini API key is not configured. Skipping live moderation.")
            return@withContext offlineModerate(text)
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Text to moderate:\n\"$text\"")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "You are MANNHALKA's real-time AI Content Moderation engine.\n" +
                                    "The user is sharing a raw emotion anonymously. Analyze the text for toxicity, explicit hate speech, " +
                                    "severe harassment, self-harm encouragement, or personally identifiable information (like emails, phone numbers, addresses, social media links/usernames).\n\n" +
                                    "Output a JSON object containing exact fields:\n" +
                                    "1. 'isSafe': boolean (true if the content is safe, false if it contains blocked content/PII)\n" +
                                    "2. 'flagReason': string (the specific reason why it is blocked, or an empty string if safe)\n\n" +
                                    "Do not output markdown code blocks. Output raw JSON.")
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API Call failed with code: ${response.code}")
                    return@withContext offlineModerate(text)
                }

                val responseBodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Response: $responseBodyStr")

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                val parts = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                val rawText = parts.getJSONObject(0).getString("text")

                val resultJson = JSONObject(rawText.trim())
                val isSafe = resultJson.getBoolean("isSafe")
                val flagReason = resultJson.getString("flagReason")

                ModerationResult(isSafe, flagReason)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini moderation: ${e.message}", e)
            offlineModerate(text)
        }
    }

    private fun offlineModerate(text: String): ModerationResult {
        // PII Detection Regex
        val piiPattern = Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b|\\b\\d{10,12}\\b")
        if (piiPattern.containsMatchIn(text)) {
            return ModerationResult(false, "Contains potential personally identifiable information (email or phone number) which would break anonymity.")
        }

        // Keywords detection
        val blockedKeywords = listOf(
            "kill myself", "suicide", "bomb", "terrorist", "nigger", "faggot", "retard", "rape"
        )
        for (word in blockedKeywords) {
            if (text.lowercase().contains(word)) {
                return ModerationResult(false, "Content contains sensitive, harmful, or toxic keywords. Let's keep this space safe for anonymous venting.")
            }
        }

        return ModerationResult(true, "")
    }
}
