package com.netraplus.utils

import android.content.Context

object Prefs {
    private const val FILE = "netraplus_prefs"
    private const val KEY_LANG = "lang"
    private const val KEY_TTS_PITCH = "tts_pitch"
    private const val KEY_TTS_SPEED = "tts_speed"

    fun setLang(context: Context, lang: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
    }

    fun getLang(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "en") ?: "en"

    fun setTtsPitch(context: Context, pitch: Float) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_TTS_PITCH, pitch).apply()
    }

    fun getTtsPitch(context: Context): Float =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getFloat(KEY_TTS_PITCH, 1.0f)

    fun setTtsSpeed(context: Context, speed: Float) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putFloat(KEY_TTS_SPEED, speed).apply()
    }

    fun getTtsSpeed(context: Context): Float =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getFloat(KEY_TTS_SPEED, 1.0f)
}
