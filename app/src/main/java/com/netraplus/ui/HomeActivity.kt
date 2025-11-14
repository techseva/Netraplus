package com.netraplus.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.netraplus.R
import com.netraplus.utils.LocaleHelper
import com.netraplus.utils.Prefs
import com.netraplus.utils.VibrationHelper
import com.netraplus.utils.TTSManager
import com.google.android.material.appbar.MaterialToolbar

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    override fun attachBaseContext(newBase: android.content.Context?) {
        if (newBase == null) { super.attachBaseContext(newBase); return }
        val lang = Prefs.getLang(newBase)
        val ctx = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }

    private lateinit var tts: TTSManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tts = TTSManager(this)
        tts.init(onReady = { tts.speak(getString(R.string.welcome_message)) })

        // Toolbar menu
        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            menu.clear()
            inflateMenu(R.menu.home_menu)
            setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_language -> {
                        VibrationHelper.vibrate(this@HomeActivity)
                        startActivity(Intent(this@HomeActivity, LanguageActivity::class.java))
                        true
                    }
                    R.id.menu_share -> {
                        VibrationHelper.vibrate(this@HomeActivity)
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                            putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name))
                        }
                        startActivity(Intent.createChooser(share, getString(R.string.app_name)))
                        true
                    }
                    R.id.menu_about -> {
                        VibrationHelper.vibrate(this@HomeActivity)
                        startActivity(Intent(this@HomeActivity, AboutActivity::class.java))
                        true
                    }
                    R.id.menu_privacy -> {
                        VibrationHelper.vibrate(this@HomeActivity)
                        startActivity(Intent(this@HomeActivity, PrivacyPolicyActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }
        listOf(
            R.id.cardVoice to "voice",
            R.id.cardVision to "vision",
            R.id.cardCommunication to "communication",
            R.id.cardEmergency to "emergency",
            R.id.cardLearning to "learning",
            R.id.cardEntertainment to "entertainment",
            R.id.cardGovSupport to "govsupport"
        ).forEach { (viewId, key) ->
            findViewById<View>(viewId).apply {
                val titleRes = when (key) {
                    "voice" -> R.string.section_voice_tools
                    "vision" -> R.string.section_vision_tools
                    "communication" -> R.string.section_communication
                    "emergency" -> R.string.section_emergency
                    "learning" -> R.string.section_learning
                    "entertainment" -> R.string.section_entertainment
                    else -> R.string.section_gov_support
                }
                contentDescription = getString(titleRes)
                setOnClickListener {
                    VibrationHelper.vibrate(this@HomeActivity)
                    tts.speak(getString(titleRes))
                    val i = Intent(this@HomeActivity, CategoryActivity::class.java)
                    i.putExtra("category", key)
                    startActivity(i)
                }
            }
        }

    }

    override fun onClick(v: View?) {}

    override fun onDestroy() {
        super.onDestroy()
        if (this::tts.isInitialized) tts.shutdown()
    }
}
