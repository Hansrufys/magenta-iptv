package com.magenta.iptv.ui.browse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.magenta.iptv.R
import com.magenta.iptv.data.model.Channel
import com.magenta.iptv.ui.playback.PlaybackActivity

class ChannelRowsFragment : Fragment() {

    companion object {
        private const val ARG_GROUP_NAMES = "group_names"
        private const val ARG_GROUP_CHANNELS = "group_channels"

        fun newInstance(groupNames: List<String>, groupChannels: List<List<Channel>>): ChannelRowsFragment {
            return ChannelRowsFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_GROUP_NAMES, ArrayList(groupNames))

                    // Serialize channels as parcelable ArrayList
                    val allChannels = ArrayList<Channel>()
                    val groupSizes = ArrayList<Int>()
                    groupChannels.forEach { group ->
                        groupSizes.add(group.size)
                        allChannels.addAll(group)
                    }
                    putIntegerArrayList("group_sizes", groupSizes)
                    putParcelableArrayList("all_channels", allChannels)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val scrollView = android.widget.ScrollView(requireContext()).apply {
            isVerticalScrollBarEnabled = false
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val linearLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val groupNames = arguments?.getStringArrayList(ARG_GROUP_NAMES) ?: arrayListOf()
        val allChannels = arguments?.getParcelableArrayList<Channel>("all_channels") ?: arrayListOf()
        val groupSizes = arguments?.getIntegerArrayList("group_sizes") ?: arrayListOf()

        var channelIndex = 0
        groupNames.forEachIndexed { index, groupName ->
            val size = groupSizes.getOrElse(index) { 0 }
            val groupChannels = allChannels.subList(channelIndex, channelIndex + size)
            channelIndex += size

            // Row header
            val headerView = inflater.inflate(R.layout.fragment_channel_row, linearLayout, false)
            headerView.findViewById<TextView>(R.id.row_title).text = groupName
            headerView.findViewById<TextView>(R.id.row_count).text = "${groupChannels.size} channels"
            linearLayout.addView(headerView)

            // Horizontal RecyclerView for channel cards
            val recyclerView = RecyclerView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 24
                }
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = ChannelCardAdapter(groupChannels) { channel ->
                    val intent = Intent(requireContext(), PlaybackActivity::class.java).apply {
                        putExtra("selectedIndex", allChannels.indexOf(channel).coerceAtLeast(0))
                    }
                    startActivity(intent)
                }
                clipToPadding = false
                setPadding(24, 0, 24, 0)
            }
            linearLayout.addView(recyclerView)
        }

        scrollView.addView(linearLayout)
        return scrollView
    }
}
