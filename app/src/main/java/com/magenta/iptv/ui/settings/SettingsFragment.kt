package com.magenta.iptv.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.magenta.iptv.R
import com.magenta.iptv.data.repository.ChannelRepository
import com.magenta.iptv.ui.browse.BrowseActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private lateinit var etM3uUrl: EditText
    private lateinit var etEpgUrl: EditText
    private lateinit var btnLoadChannels: Button
    private lateinit var progressBar: ProgressBar

    private val prefsName = "iptv_prefs"
    private val keyM3uUrl = "pref_m3u_url"
    private val keyEpgUrl = "pref_epg_url"
    private val keyChannelsLoaded = "channels_loaded"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etM3uUrl = view.findViewById(R.id.et_m3u_url)
        etEpgUrl = view.findViewById(R.id.et_epg_url)
        btnLoadChannels = view.findViewById(R.id.btn_load_channels)
        progressBar = view.findViewById(R.id.progress_bar)

        loadSavedUrls()

        btnLoadChannels.setOnClickListener {
            val m3uUrl = etM3uUrl.text.toString().trim()
            val epgUrl = etEpgUrl.text.toString().trim()

            // Validate M3U URL
            if (m3uUrl.isEmpty() || (!m3uUrl.startsWith("http://") && !m3uUrl.startsWith("https://"))) {
                Toast.makeText(requireContext(), "Please enter a valid M3U URL (must start with http:// or https://)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Save URLs
            saveUrls(m3uUrl, epgUrl)

            // Show progress, disable button
            progressBar.visibility = View.VISIBLE
            btnLoadChannels.isEnabled = false

            // Fetch channels
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val repository = ChannelRepository()
                    val result = withContext(Dispatchers.IO) {
                        repository.fetchChannels(m3uUrl)
                    }

                    progressBar.visibility = View.GONE
                    btnLoadChannels.isEnabled = true

                    result.onSuccess { channels ->
                        if (!isAdded) return@onSuccess
                        // Mark channels as loaded
                        requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(keyChannelsLoaded, true)
                            .apply()

                        // Start BrowseActivity with channel list
                        val intent = Intent(requireContext(), BrowseActivity::class.java).apply {
                            putParcelableArrayListExtra("channels", ArrayList(channels))
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                    }.onFailure { error ->
                        if (!isAdded) return@onFailure
                        Toast.makeText(
                            requireContext(),
                            "Failed to load channels: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    btnLoadChannels.isEnabled = true
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.localizedMessage ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun loadSavedUrls() {
        val prefs = requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        etM3uUrl.setText(prefs.getString(keyM3uUrl, ""))
        etEpgUrl.setText(prefs.getString(keyEpgUrl, ""))
    }

    private fun saveUrls(m3uUrl: String, epgUrl: String) {
        requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString(keyM3uUrl, m3uUrl)
            .putString(keyEpgUrl, epgUrl)
            .apply()
    }
}
