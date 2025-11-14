package com.netraplus.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TTSManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    fun init(locale: Locale = Locale.getDefault(), onReady: (() -> Unit)? = null) {
        tts = TextToSpeech(context) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                setLanguage(locale)
                onReady?.let { it() }
            }
        }
    }

    fun setLanguage(locale: Locale) {
        tts?.language = locale
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, System.currentTimeMillis().toString())
    }

    fun speak(text: String, onDone: (() -> Unit)?) {
        if (!ready) return
        val utteranceId = System.currentTimeMillis().toString()
        if (onDone != null) {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceIdParam: String?) {}
                override fun onError(utteranceIdParam: String?) {}
                override fun onDone(utteranceIdParam: String?) {
                    if (utteranceIdParam == utteranceId) onDone()
                }
            })
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        ready = false
    }
}
