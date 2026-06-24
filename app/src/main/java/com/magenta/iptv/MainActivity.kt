package com.magenta.iptv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.magenta.iptv.data.ChannelStore
import com.magenta.iptv.data.PlaylistUrls
import com.magenta.iptv.data.repository.ChannelRepository
import com.magenta.iptv.ui.browse.BrowseActivity
import com.magenta.iptv.ui.settings.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("iptv_prefs", MODE_PRIVATE)
        val m3uUrl = prefs.getString("pref_m3u_url", "") ?: ""
        val channelsLoaded = prefs.getBoolean("channels_loaded", false)

        // If channels were already loaded previously, go straight to Browse
        if (channelsLoaded && ChannelStore.hasChannels(this)) {
            startActivity(Intent(this, BrowseActivity::class.java))
            finish()
            return
        }

        val progressBar = findViewById<ProgressBar>(R.id.main_progress)
        val statusText = findViewById<TextView>(R.id.main_status)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val repository = ChannelRepository()
            val urlsToFetch = if (m3uUrl.isNotEmpty()) {
                // User provided a custom URL — use that plus defaults as fallback
                listOf(m3uUrl) + PlaylistUrls.allDefaultUrls
            } else {
                // No custom URL — use hardcoded Arabic defaults
                PlaylistUrls.allDefaultUrls
            }

            if (statusText != null) {
                statusText.text = "Loading Arabic channels..."
                statusText.visibility = View.VISIBLE
            }

            val result = repository.fetchChannelsFromMultiple(urlsToFetch)

            progressBar.visibility = View.GONE
            if (statusText != null) statusText.visibility = View.GONE

            result.onSuccess { channels ->
                if (channels.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "No channels found. Going to Settings.",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    finish()
                    return@launch
                }

                ChannelStore.save(this@MainActivity, channels)
                prefs.edit()
                    .putBoolean("channels_loaded", true)
                    .apply()

                Toast.makeText(
                    this@MainActivity,
                    "Loaded ${channels.size} channels",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this@MainActivity, BrowseActivity::class.java))
                finish()
            }.onFailure { error ->
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load channels: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                // Fallback to settings
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                finish()
            }
        }
    }
}
