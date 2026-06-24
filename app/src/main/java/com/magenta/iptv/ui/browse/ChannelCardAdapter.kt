package com.magenta.iptv.ui.browse

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.magenta.iptv.R
import com.magenta.iptv.data.model.Channel

class ChannelCardAdapter(
    private val channels: List<Channel>,
    private val onClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelCardAdapter.CardViewHolder>() {

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.card_logo)
        val name: TextView = view.findViewById(R.id.card_name)
        val group: TextView = view.findViewById(R.id.card_group)
        val cardBg: View = view.findViewById(R.id.card_bg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val channel = channels[position]

        holder.name.text = channel.name
        holder.group.text = channel.groupTitle ?: "Uncategorized"

        if (!channel.logoUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(channel.logoUrl)
                .placeholder(R.drawable.tv_banner)
                .into(holder.logo)
        } else {
            holder.logo.setImageResource(R.drawable.tv_banner)
        }

        holder.itemView.setOnClickListener { onClick(channel) }

        holder.itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                holder.cardBg.setBackgroundResource(R.drawable.bg_card_glass_selected)
                holder.itemView.scaleX = 1.08f
                holder.itemView.scaleY = 1.08f
                holder.itemView.translationZ = 8f
            } else {
                holder.cardBg.setBackgroundResource(R.drawable.bg_card_glass)
                holder.itemView.scaleX = 1.0f
                holder.itemView.scaleY = 1.0f
                holder.itemView.translationZ = 0f
            }
        }
    }

    override fun getItemCount() = channels.size
}
