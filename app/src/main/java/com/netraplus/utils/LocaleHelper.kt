package com.netraplus.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun applyLocale(context: Context, lang: String): Context {
        val locale = when (lang) {
            "te" -> Locale("te")
            "hi" -> Locale("hi")
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
