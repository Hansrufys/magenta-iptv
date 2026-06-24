package com.magenta.iptv.ui.playback

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.magenta.iptv.R
import com.magenta.iptv.data.ChannelStore
import com.magenta.iptv.data.model.Channel
import com.magenta.iptv.data.model.Programme
import com.magenta.iptv.data.repository.ChannelRepository
import kotlinx.coroutines.launch

class PlaybackFragment : Fragment() {

    companion object {
        private const val ARG_SELECTED_INDEX = "selectedIndex"
        private const val INFO_OVERLAY_DURATION_MS = 3000L

        fun newInstance(selectedIndex: Int): PlaybackFragment {
            val fragment = PlaybackFragment()
            val args = Bundle()
            args.putInt(ARG_SELECTED_INDEX, selectedIndex)
            fragment.arguments = args
            return fragment
        }
    }

    private var exoPlayer: ExoPlayer? = null
    private var channels: List<Channel> = emptyList()
    private var currentIndex: Int = 0
    private var epgData: Map<String, Programme> = emptyMap()

    private lateinit var playerView: PlayerView
    private lateinit var infoOverlay: TextView
    private lateinit var epgOverlay: EpgOverlayView
    private val handler = Handler(Looper.getMainLooper())
    private val hideInfoOverlay = Runnable {
        infoOverlay.visibility = View.GONE
    }

    private val channelRepository = ChannelRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channels = ChannelStore.load(requireContext())
        currentIndex = arguments?.getInt(ARG_SELECTED_INDEX, 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerView = view.findViewById(R.id.player_view)
        infoOverlay = view.findViewById(R.id.info_overlay)
        epgOverlay = view.findViewById(R.id.epg_overlay)

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        switchChannel(-1)
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        showEpgOverlay()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        loadEpg()
        initializePlayer()
    }

    private fun loadEpg() {
        val prefs = activity?.getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
        val epgUrl = prefs?.getString("pref_epg_url", null)
        if (epgUrl.isNullOrBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            channelRepository.fetchEpg(epgUrl)
                .onSuccess { epg ->
                    epgData = epg
                }
                .onFailure {
                }
        }
    }

    private fun initializePlayer() {
        if (channels.isEmpty()) {
            Toast.makeText(activity, "No channels available", Toast.LENGTH_SHORT).show()
            activity?.finish()
            return
        }

        val ctx = context ?: return
        val player = ExoPlayer.Builder(ctx).build()
        exoPlayer = player

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(
                    activity,
                    "Playback error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        })

        playerView.player = player

        playChannel(currentIndex)
    }

    private fun playChannel(index: Int) {
        if (index < 0 || index >= channels.size) return
        val channel = channels[index]
        val mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(channel.streamUrl))

        exoPlayer?.setMediaSource(mediaSource)
        exoPlayer?.prepare()

        showInfoOverlay(channel)
        showCurrentEpg(channel)
    }

    private fun switchChannel(direction: Int) {
        if (channels.isEmpty()) return

        currentIndex = (currentIndex + direction + channels.size) % channels.size
        playChannel(currentIndex)
    }

    private fun showInfoOverlay(channel: Channel) {
        handler.removeCallbacks(hideInfoOverlay)
        infoOverlay.text = buildString {
            append(channel.name)
            if (!channel.groupTitle.isNullOrBlank()) {
                append("\n")
                append(channel.groupTitle)
            }
        }
        infoOverlay.visibility = View.VISIBLE
        handler.postDelayed(hideInfoOverlay, INFO_OVERLAY_DURATION_MS)
    }

    private fun showCurrentEpg(channel: Channel) {
        val channelId = channel.epgId ?: return
        epgOverlay.show(channelId, epgData)
    }

    private fun showEpgOverlay() {
        if (channels.isEmpty()) return
        val channel = channels[currentIndex]
        val channelId = channel.epgId ?: return
        epgOverlay.show(channelId, epgData)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(hideInfoOverlay)
        epgOverlay.hide()
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroyView()
    }
}
