package com.netraplus.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.netraplus.R
import com.netraplus.utils.Prefs
import com.netraplus.utils.VibrationHelper
import com.netraplus.utils.TTSManager

class LanguageActivity : AppCompatActivity() {
    private lateinit var tts: TTSManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)
        tts = TTSManager(this)
        tts.init()

        val btnEn: Button = findViewById(R.id.btnEnglish)
        val btnTe: Button = findViewById(R.id.btnTelugu)
        val btnHi: Button = findViewById(R.id.btnHindi)

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.btnEnglish -> {
                    Prefs.setLang(this, "en")
                    tts.speak(getString(R.string.lang_english))
                }
                R.id.btnTelugu -> {
                    Prefs.setLang(this, "te")
                    tts.speak(getString(R.string.lang_telugu))
                }
                R.id.btnHindi -> {
                    Prefs.setLang(this, "hi")
                    tts.speak(getString(R.string.lang_hindi))
                }
            }
            VibrationHelper.vibrate(this)
            tts.speak(getString(R.string.action_select_language))
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        btnEn.setOnClickListener(click)
        btnTe.setOnClickListener(click)
        btnHi.setOnClickListener(click)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::tts.isInitialized) tts.shutdown()
    }
}
