package com.magenta.iptv.data.repository

import com.magenta.iptv.data.model.Channel
import com.magenta.iptv.data.model.Programme
import com.magenta.iptv.data.parser.M3uParser
import com.magenta.iptv.data.parser.XmltvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ChannelRepository {
    private var cachedChannels: List<Channel> = emptyList()
    private var cachedEpg: Map<String, Programme> = emptyMap()

    suspend fun fetchChannels(url: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val channels = M3uParser.parse(body)
            cachedChannels = channels
            Result.success(channels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchEpg(url: String): Result<Map<String, Programme>> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(Exception("HTTP ${response.code}"))
            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
            val epg = XmltvParser.parse(body)
            cachedEpg = epg
            Result.success(epg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCachedChannels(): List<Channel> = cachedChannels

    fun getCachedEpg(): Map<String, Programme> = cachedEpg
}
