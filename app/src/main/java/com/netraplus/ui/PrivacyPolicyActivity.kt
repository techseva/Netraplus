package com.netraplus.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netraplus.R

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        title = getString(R.string.menu_privacy)
    }
}
