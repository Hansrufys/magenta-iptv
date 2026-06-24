package com.magenta.iptv.ui.playback

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.magenta.iptv.R
import com.magenta.iptv.data.model.Channel

class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)

        if (savedInstanceState == null) {
            val channels = intent.getParcelableArrayListExtra<Channel>("channels")
                ?: run {
                    finish()
                    return
                }
            val selectedIndex = intent.getIntExtra("selectedIndex", 0)

            val fragment = PlaybackFragment.newInstance(channels, selectedIndex)
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
