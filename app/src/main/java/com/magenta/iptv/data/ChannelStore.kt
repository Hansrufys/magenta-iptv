package com.magenta.iptv.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.magenta.iptv.data.model.Channel
import java.io.File

object ChannelStore {
    private const val FILE_NAME = "channels.json"
    private val gson = Gson()

    fun save(context: Context, channels: List<Channel>) {
        val file = File(context.filesDir, FILE_NAME)
        file.writeText(gson.toJson(channels))
    }

    fun load(context: Context): List<Channel> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val type = object : TypeToken<List<Channel>>() {}.type
        return gson.fromJson(file.readText(), type) ?: emptyList()
    }

    fun clear(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) file.delete()
    }
}
