package com.example.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceManager(context: Context, private val onInit: (Int) -> Unit) {
    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        onInit(status)
    }

    init {
        tts.language = Locale.US
    }

    fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
