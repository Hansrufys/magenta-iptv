package com.magenta.iptv.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import com.magenta.iptv.data.ChannelStore
import com.magenta.iptv.data.model.Channel
import com.magenta.iptv.ui.playback.PlaybackActivity

class ChannelBrowserFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val channels = ChannelStore.load(requireContext())

        val grouped = channels.groupBy { it.groupTitle ?: "Uncategorized" }

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        var headerIndex = 0L
        for ((groupName, groupChannels) in grouped) {
            val header = HeaderItem(headerIndex, groupName)
            val listRowAdapter = ArrayObjectAdapter(CardPresenter())
            listRowAdapter.addAll(0, groupChannels)
            rowsAdapter.add(ListRow(header, listRowAdapter))
            headerIndex++
        }

        adapter = rowsAdapter

        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Channel) {
                val index = channels.indexOf(item)
                val intent = Intent(requireContext(), PlaybackActivity::class.java).apply {
                    putExtra("selectedIndex", if (index >= 0) index else 0)
                }
                startActivity(intent)
            }
        }
    }
}
