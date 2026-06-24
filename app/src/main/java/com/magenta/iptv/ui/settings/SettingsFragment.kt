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
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.magenta.iptv.R
import com.magenta.iptv.data.ChannelStore
import com.magenta.iptv.data.PlaylistUrls
import com.magenta.iptv.data.repository.ChannelRepository
import com.magenta.iptv.ui.browse.BrowseActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private lateinit var etM3uUrl: EditText
    private lateinit var etEpgUrl: EditText
    private lateinit var btnLoadChannels: Button
    private lateinit var btnReset: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvDefaultSources: TextView

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
        btnReset = view.findViewById(R.id.btn_reset)
        progressBar = view.findViewById(R.id.progress_bar)
        tvDefaultSources = view.findViewById(R.id.tv_default_sources)

        // Show default Arabic playlist sources
        val sourcesText = PlaylistUrls.defaultSources.joinToString("\n") { source ->
            "• ${source.name}: ${source.url}"
        }
        tvDefaultSources.text = sourcesText

        loadSavedUrls()

        btnLoadChannels.setOnClickListener {
            val m3uUrl = etM3uUrl.text.toString().trim()
            val epgUrl = etEpgUrl.text.toString().trim()

            // Save URLs
            saveUrls(m3uUrl, epgUrl)

            // Show progress, disable button
            progressBar.visibility = View.VISIBLE
            btnLoadChannels.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val repository = ChannelRepository()
                    val urlsToFetch = if (m3uUrl.isNotEmpty()) {
                        listOf(m3uUrl) + PlaylistUrls.allDefaultUrls
                    } else {
                        PlaylistUrls.allDefaultUrls
                    }

                    val result = repository.fetchChannelsFromMultiple(urlsToFetch)

                    progressBar.visibility = View.GONE
                    btnLoadChannels.isEnabled = true

                    result.onSuccess { channels ->
                        if (!isAdded) return@onSuccess
                        ChannelStore.save(requireContext(), channels)
                        requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(keyChannelsLoaded, true)
                            .apply()

                        Toast.makeText(
                            requireContext(),
                            "Loaded ${channels.size} channels",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(requireContext(), BrowseActivity::class.java).apply {
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

        btnReset.setOnClickListener {
            // Clear saved URL and reload from defaults
            requireContext().getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                .edit()
                .remove(keyM3uUrl)
                .putBoolean(keyChannelsLoaded, false)
                .apply()

            etM3uUrl.setText("")

            // Clear stored channels so they reload from defaults
            ChannelStore.save(requireContext(), emptyList())

            Toast.makeText(requireContext(), "Reset to default Arabic channels", Toast.LENGTH_SHORT).show()

            // Restart the app by going to MainActivity
            val intent = Intent(requireContext(), requireContext().javaClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
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
