package com.magenta.iptv.ui.settings

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.magenta.iptv.R

class SettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        finish()
    }
}
