package com.magenta.iptv.ui.playback

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.magenta.iptv.R

class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)

        if (savedInstanceState == null) {
            val selectedIndex = intent.getIntExtra("selectedIndex", 0)
            val fragment = PlaybackFragment.newInstance(selectedIndex)
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
