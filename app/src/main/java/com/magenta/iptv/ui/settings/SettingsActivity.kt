package com.magenta.iptv.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.magenta.iptv.R
import com.magenta.iptv.ui.browse.BrowseActivity

class SettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        val prefs = getSharedPreferences("iptv_prefs", MODE_PRIVATE)
        val hasChannels = prefs.getBoolean("channels_loaded", false)
        if (hasChannels) {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }
}
