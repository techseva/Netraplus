package com.netraplus.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netraplus.R
import com.netraplus.utils.Prefs
import com.netraplus.utils.TTSManager
import com.netraplus.utils.LocaleHelper
import java.util.Locale

class SplashActivity : AppCompatActivity() {
    private lateinit var tts: TTSManager

    override fun attachBaseContext(newBase: android.content.Context?) {
        if (newBase == null) { super.attachBaseContext(newBase); return }
        val lang = Prefs.getLang(newBase)
        val ctx = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        tts = TTSManager(this)
        // Use the current resources locale for TTS
        val locale: Locale = resources.configuration.locales[0]
        tts.init(locale = locale, onReady = {
            tts.speak(getString(R.string.tts_welcome)) {
                runOnUiThread {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
