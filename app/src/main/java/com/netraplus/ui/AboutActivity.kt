package com.netraplus.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netraplus.R
import com.google.android.material.appbar.MaterialToolbar
import com.netraplus.utils.VibrationHelper

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.navigationContentDescription = getString(R.string.action_back)
        toolbar.setNavigationOnClickListener {
            VibrationHelper.vibrate(this)
            finish()
        }
    }
}
