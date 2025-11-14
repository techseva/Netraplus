package com.netraplus.calculator

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.netraplus.R
import com.netraplus.utils.SpeechHelper

/**
 * Settings activity for calculator voice and haptic feedback.
 * Allows adjustment of TTS pitch, speed, and haptic feedback toggle.
 */
class CalculatorVoiceSettingsActivity : AppCompatActivity() {

    private lateinit var seekPitch: SeekBar
    private lateinit var seekSpeed: SeekBar
    private lateinit var switchHaptic: Switch
    private lateinit var tvPitchValue: TextView
    private lateinit var tvSpeedValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator_voice_settings)

        // Initialize TTS
        SpeechHelper.init(this)

        setupToolbar()
        initializeViews()
        loadSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.title = getString(R.string.calc_voice_settings_title)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initializeViews() {
        seekPitch = findViewById(R.id.seekPitch)
        seekSpeed = findViewById(R.id.seekSpeed)
        switchHaptic = findViewById(R.id.switchHaptic)
        tvPitchValue = findViewById(R.id.tvPitchValue)
        tvSpeedValue = findViewById(R.id.tvSpeedValue)

        // Set up focus listeners for accessibility
        seekPitch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                SpeechHelper.speak(getString(R.string.calc_cursor_pitch))
            }
        }

        seekSpeed.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                SpeechHelper.speak(getString(R.string.calc_cursor_speed))
            }
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
        
        // Load pitch (0.5 to 2.0, mapped to 0-150)
        val pitch = SpeechHelper.getPitch(this)
        val pitchProgress = ((pitch - 0.5f) * 100).toInt()
        seekPitch.max = 150
        seekPitch.progress = pitchProgress.coerceIn(0, 150)
        updatePitchDisplay(pitch)

        // Load speed (0.5 to 2.0, mapped to 0-150)
        val speed = SpeechHelper.getSpeed(this)
        val speedProgress = ((speed - 0.5f) * 100).toInt()
        seekSpeed.max = 150
        seekSpeed.progress = speedProgress.coerceIn(0, 150)
        updateSpeedDisplay(speed)

        // Load haptic setting
        hapticEnabled = prefs.getBoolean("haptic_enabled", true)
        switchHaptic.isChecked = hapticEnabled
    }

    private fun setupListeners() {
        seekPitch.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val pitch = 0.5f + (progress / 100f)
                    updatePitchDisplay(pitch)
                    SpeechHelper.setPitch(pitch)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val pitch = 0.5f + (seekPitch.progress / 100f)
                SpeechHelper.saveSettings(this@CalculatorVoiceSettingsActivity, pitch, SpeechHelper.getSpeed(this@CalculatorVoiceSettingsActivity))
            }
        })

        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val speed = 0.5f + (progress / 100f)
                    updateSpeedDisplay(speed)
                    SpeechHelper.setSpeechRate(speed)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val speed = 0.5f + (seekSpeed.progress / 100f)
                SpeechHelper.saveSettings(this@CalculatorVoiceSettingsActivity, SpeechHelper.getPitch(this@CalculatorVoiceSettingsActivity), speed)
            }
        })

        switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            hapticEnabled = isChecked
            val prefs = getSharedPreferences("calc_voice_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("haptic_enabled", isChecked).apply()
        }
    }

    private fun updatePitchDisplay(pitch: Float) {
        tvPitchValue.text = String.format("%.1fx", pitch)
    }

    private fun updateSpeedDisplay(speed: Float) {
        tvSpeedValue.text = String.format("%.1fx", speed)
    }

    private var hapticEnabled = true

    override fun onDestroy() {
        super.onDestroy()
        SpeechHelper.stop()
    }
}

