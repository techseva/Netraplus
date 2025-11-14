package com.netraplus.ui.calculator

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.netraplus.R
import com.netraplus.utils.Prefs
import com.netraplus.utils.VibrationHelper

class CalculatorSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc_settings)

        findViewById<MaterialToolbar>(R.id.topAppBar).apply {
            title = getString(R.string.calc_settings_title)
            setNavigationIcon(R.drawable.ic_back)
            navigationContentDescription = getString(R.string.action_back)
            setNavigationOnClickListener { finish() }
        }

        val tvPitch = findViewById<TextView>(R.id.tvPitchValue)
        val tvSpeed = findViewById<TextView>(R.id.tvSpeedValue)
        val seekPitch = findViewById<SeekBar>(R.id.seekPitch)
        val seekSpeed = findViewById<SeekBar>(R.id.seekSpeed)

        fun Float.toDisplay(): String = String.format("%.1fx", this)

        val initPitch = Prefs.getTtsPitch(this)
        val initSpeed = Prefs.getTtsSpeed(this)
        seekPitch.max = 150
        seekSpeed.max = 150
        seekPitch.progress = ((initPitch - 0.5f) * 100).toInt()
        seekSpeed.progress = ((initSpeed - 0.5f) * 100).toInt()
        tvPitch.text = initPitch.toDisplay()
        tvSpeed.text = initSpeed.toDisplay()

        seekPitch.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = 0.5f + (progress / 100f)
                tvPitch.text = value.toDisplay()
                Prefs.setTtsPitch(this@CalculatorSettingsActivity, value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) { VibrationHelper.vibrate(this@CalculatorSettingsActivity) }
        })

        seekSpeed.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = 0.5f + (progress / 100f)
                tvSpeed.text = value.toDisplay()
                Prefs.setTtsSpeed(this@CalculatorSettingsActivity, value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) { VibrationHelper.vibrate(this@CalculatorSettingsActivity) }
        })
    }
}
