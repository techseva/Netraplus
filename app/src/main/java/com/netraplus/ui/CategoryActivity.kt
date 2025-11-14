package com.netraplus.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.netraplus.R
import com.netraplus.utils.LocaleHelper
import com.netraplus.utils.Prefs
import com.netraplus.utils.VibrationHelper
import com.netraplus.viewmodel.CategoryViewModel
import com.netraplus.utils.TTSManager
import com.google.android.material.appbar.MaterialToolbar
import com.netraplus.data.ServiceItem

class CategoryActivity : AppCompatActivity() {

    private val vm: CategoryViewModel by viewModels()
    private lateinit var tts: TTSManager
    private var itemsList: List<ServiceItem> = emptyList()

    override fun attachBaseContext(newBase: android.content.Context?) {
        if (newBase == null) { super.attachBaseContext(newBase); return }
        val lang = Prefs.getLang(newBase)
        val ctx = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        tts = TTSManager(this)
        tts.init()
        val listView: ListView = findViewById(R.id.listServices)
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        val adapter = ArrayAdapter<String>(this, R.layout.item_service, R.id.tvItem, mutableListOf())
        listView.adapter = adapter
        val key = intent.getStringExtra("category") ?: "voice"
        val titleRes = when (key) {
            "voice" -> R.string.section_voice_tools
            "vision" -> R.string.section_vision_tools
            "communication" -> R.string.section_communication
            "emergency" -> R.string.section_emergency
            "learning" -> R.string.section_learning
            "entertainment" -> R.string.section_entertainment
            "govsupport" -> R.string.section_gov_support
            // legacy fallbacks
            "government" -> R.string.cat_government
            "health" -> R.string.cat_health
            "education" -> R.string.cat_education
            "jobs" -> R.string.cat_jobs
            "banking" -> R.string.cat_banking
            "daily" -> R.string.cat_daily
            else -> R.string.title_category
        }
        toolbar.title = getString(titleRes)
        toolbar.navigationContentDescription = getString(R.string.action_back)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            VibrationHelper.vibrate(this)
            finish()
        }
        vm.items.observe(this) { items ->
            itemsList = items
            adapter.clear()
            adapter.addAll(items.map { it.title })
            adapter.notifyDataSetChanged()
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            VibrationHelper.vibrate(this)
            val item = itemsList.getOrNull(position)
            if (item != null) {
                if (key == "voice" && (item.id == "talk_calc" || item.title.equals(getString(R.string.calc_title), ignoreCase = true))) {
                    try {
                        startActivity(Intent(this, com.netraplus.calculator.TalkingCalculatorActivity::class.java))
                    } catch (e: Exception) {
                        try {
                            startActivity(Intent(this, com.netraplus.ui.calculator.CalculatorActivity::class.java))
                        } catch (e2: Exception) {
                            android.widget.Toast.makeText(this, "Unable to open Talking Calculator", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    tts.speak(item.title)
                }
            }
        }
        vm.load(key)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::tts.isInitialized) tts.shutdown()
    }
}
