package com.magenta.iptv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.magenta.iptv.data.repository.ChannelRepository
import com.magenta.iptv.ui.browse.BrowseActivity
import com.magenta.iptv.ui.settings.SettingsActivity
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("iptv_prefs", MODE_PRIVATE)
        val m3uUrl = prefs.getString("pref_m3u_url", "") ?: ""

        if (m3uUrl.isNotEmpty()) {
            // URL exists — fetch channels and navigate to BrowseActivity
            val progressBar = findViewById<ProgressBar>(R.id.main_progress)
            progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                val repository = ChannelRepository()
                val result = repository.fetchChannels(m3uUrl)

                progressBar.visibility = View.GONE

                result.onSuccess { channels ->
                    if (channels.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "No channels found in playlist",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                    val intent = Intent(this@MainActivity, BrowseActivity::class.java).apply {
                        putParcelableArrayListExtra("channels", ArrayList(channels))
                    }
                    startActivity(intent)
                    finish()
                }.onFailure { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to load channels: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            // No URL configured — go to settings
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
