package com.netraplus.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Singleton helper for Text-to-Speech functionality.
 * Manages TTS initialization, pitch, speed, and locale settings.
 */
object SpeechHelper {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var initializationListener: (() -> Unit)? = null

    /**
     * Initialize TTS engine with the given context.
     * @param context Application context
     * @param onReady Callback invoked when TTS is ready
     */
    fun init(context: Context, onReady: (() -> Unit)? = null) {
        if (tts != null && isInitialized) {
            onReady?.invoke()
            return
        }

        initializationListener = onReady
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                configureTTS(context)
                initializationListener?.invoke()
            }
        }
    }

    /**
     * Configure TTS with saved preferences and locale.
     */
    private fun configureTTS(context: Context) {
        val prefs = context.getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
        val pitch = prefs.getFloat("pitch", 1.0f)
        val speed = prefs.getFloat("speed", 1.0f)

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)

        // Set locale based on system or preferences
        val locale = getPreferredLocale(context)
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English
            tts?.setLanguage(Locale.ENGLISH)
        }
    }

    /**
     * Get preferred locale for TTS.
     * Checks system locale and supports Telugu, Hindi, and English.
     */
    private fun getPreferredLocale(context: Context): Locale {
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }

        return when (systemLocale.language) {
            "te" -> Locale("te", "IN") // Telugu
            "hi" -> Locale("hi", "IN") // Hindi
            else -> Locale.ENGLISH
        }
    }

    /**
     * Speak the given text.
     * @param text Text to speak
     * @param utteranceId Unique ID for this utterance (optional)
     */
    fun speak(text: String, utteranceId: String? = null) {
        if (!isInitialized || tts == null) return

        val id = utteranceId ?: System.currentTimeMillis().toString()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    /**
     * Set pitch (0.5 to 2.0).
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /**
     * Set speech rate (0.5 to 2.0).
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /**
     * Save pitch and speed to preferences.
     */
    fun saveSettings(context: Context, pitch: Float, speed: Float) {
        val prefs = context.getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat("pitch", pitch)
            .putFloat("speed", speed)
            .apply()
        
        setPitch(pitch)
        setSpeechRate(speed)
    }

    /**
     * Get saved pitch value.
     */
    fun getPitch(context: Context): Float {
        val prefs = context.getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
        return prefs.getFloat("pitch", 1.0f)
    }

    /**
     * Get saved speed value.
     */
    fun getSpeed(context: Context): Float {
        val prefs = context.getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
        return prefs.getFloat("speed", 1.0f)
    }

    /**
     * Check if TTS is ready.
     */
    fun isReady(): Boolean = isInitialized && tts != null

    /**
     * Stop current speech.
     */
    fun stop() {
        tts?.stop()
    }

    /**
     * Shutdown TTS engine.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}

