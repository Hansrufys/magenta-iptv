package com.magenta.iptv.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.magenta.iptv.R
import com.magenta.iptv.data.ChannelStore
import com.magenta.iptv.data.model.Channel
import com.magenta.iptv.ui.playback.PlaybackActivity
import com.magenta.iptv.ui.settings.SettingsActivity

class BrowseActivity : FragmentActivity() {

    private lateinit var channels: List<Channel>
    private lateinit var sidebar: LinearLayout
    private lateinit var navBrowse: LinearLayout
    private lateinit var navWatchlist: LinearLayout
    private lateinit var navComingSoon: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)

        channels = ChannelStore.load(this)

        sidebar = findViewById(R.id.sidebar)
        navBrowse = findViewById(R.id.nav_browse)
        navWatchlist = findViewById(R.id.nav_watchlist)
        navComingSoon = findViewById(R.id.nav_coming_soon)
        navSettings = findViewById(R.id.nav_settings)

        setupSidebar()
        setupHeroBanner()
        setupChannelRows()
    }

    private fun setupSidebar() {
        val navItems = listOf(navBrowse, navWatchlist, navComingSoon, navSettings)

        navBrowse.setOnClickListener { selectNav(navBrowse, navItems) }
        navWatchlist.setOnClickListener { selectNav(navWatchlist, navItems) }
        navComingSoon.setOnClickListener { selectNav(navComingSoon, navItems) }
        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        navBrowse.isSelected = true
        updateNavColors(navBrowse, true)
    }

    private fun selectNav(selected: LinearLayout, all: List<LinearLayout>) {
        all.forEach { updateNavColors(it, it == selected) }
    }

    private fun updateNavColors(item: LinearLayout, selected: Boolean) {
        val icon = item.getChildAt(0) as? ImageView
        val text = item.getChildAt(1) as? TextView
        if (selected) {
            item.isSelected = true
            icon?.setColorFilter(getColor(R.color.netflix_white))
            text?.setTextColor(getColor(R.color.netflix_white))
        } else {
            item.isSelected = false
            icon?.setColorFilter(getColor(R.color.netflix_text))
            text?.setTextColor(getColor(R.color.netflix_text))
        }
    }

    private fun setupHeroBanner() {
        if (channels.isEmpty()) return

        val heroContainer = findViewById<android.widget.FrameLayout>(R.id.hero_container)
        val heroFragment = HeroBannerFragment.newInstance(channels.random())
        supportFragmentManager.beginTransaction()
            .replace(R.id.hero_container, heroFragment)
            .commit()

        heroContainer.setOnClickListener {
            if (channels.isNotEmpty()) {
                val index = channels.indexOf(channels.random())
                val intent = Intent(this, PlaybackActivity::class.java).apply {
                    putExtra("selectedIndex", if (index >= 0) index else 0)
                }
                startActivity(intent)
            }
        }
    }

    private fun setupChannelRows() {
        if (channels.isEmpty()) return

        val rowsContainer = findViewById<android.widget.FrameLayout>(R.id.rows_container)
        val grouped = channels.groupBy { it.groupTitle ?: "Uncategorized" }

        // Show first 6 groups max
        val topGroups = grouped.entries.take(6)

        val rowsFragment = ChannelRowsFragment.newInstance(
            topGroups.map { it.key },
            topGroups.map { it.value.map { ch -> ch } }
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.rows_container, rowsFragment)
            .commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_SETTINGS -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
    }
}
