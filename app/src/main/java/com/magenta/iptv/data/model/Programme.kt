package com.magenta.iptv.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Programme(
    val channelId: String,
    val title: String,
    val startTime: Long, // epoch millis
    val endTime: Long    // epoch millis
) : Parcelable
