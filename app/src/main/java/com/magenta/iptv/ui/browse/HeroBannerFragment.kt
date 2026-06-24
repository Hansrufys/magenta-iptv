package com.magenta.iptv.ui.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.magenta.iptv.R
import com.magenta.iptv.data.model.Channel

class HeroBannerFragment : Fragment() {

    companion object {
        private const val ARG_CHANNEL_NAME = "channel_name"
        private const val ARG_CHANNEL_GROUP = "channel_group"
        private const val ARG_CHANNEL_LOGO = "channel_logo"

        fun newInstance(channel: Channel): HeroBannerFragment {
            return HeroBannerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHANNEL_NAME, channel.name)
                    putString(ARG_CHANNEL_GROUP, channel.groupTitle ?: "Uncategorized")
                    putString(ARG_CHANNEL_LOGO, channel.logoUrl)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hero_banner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString(ARG_CHANNEL_NAME) ?: ""
        val group = arguments?.getString(ARG_CHANNEL_GROUP) ?: ""
        val logoUrl = arguments?.getString(ARG_CHANNEL_LOGO) ?: ""

        view.findViewById<TextView>(R.id.hero_name).text = name
        view.findViewById<TextView>(R.id.hero_group).text = group.uppercase()

        val logoView = view.findViewById<ImageView>(R.id.hero_logo)
        if (logoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.tv_banner)
                .into(logoView)
        }
    }
}
