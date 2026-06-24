package com.magenta.iptv.data.repository

import com.magenta.iptv.data.model.Channel
import com.magenta.iptv.data.model.Programme
import com.magenta.iptv.data.parser.M3uParser
import com.magenta.iptv.data.parser.XmltvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ChannelRepository {
    private var cachedChannels: List<Channel> = emptyList()
    private var cachedEpg: Map<String, Programme> = emptyMap()

    private fun buildClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Fetch channels from a single M3U URL.
     */
    suspend fun fetchChannels(url: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        try {
            val client = buildClient()
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

    /**
     * Fetch channels from multiple URLs concurrently, merge and deduplicate by name.
     * Returns all channels that could be fetched successfully. Fails only if ALL URLs fail.
     */
    suspend fun fetchChannelsFromMultiple(urls: List<String>): Result<List<Channel>> {
        val results = coroutineScope {
            urls.map { url ->
                async(Dispatchers.IO) {
                    try {
                        val client = buildClient()
                        val request = Request.Builder().url(url).build()
                        val response = client.newCall(request).execute()
                        if (!response.isSuccessful) return@async emptyList()
                        val body = response.body?.string() ?: return@async emptyList()
                        M3uParser.parse(body)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }.awaitAll()
        }

        val merged = results.flatten()
        if (merged.isEmpty() && urls.isNotEmpty()) {
            return Result.failure(Exception("All ${urls.size} playlist sources failed"))
        }

        // Deduplicate by channel name (keep first occurrence)
        val seen = mutableSetOf<String>()
        val deduplicated = mutableListOf<Channel>()
        for (channel in merged) {
            val key = channel.name.trim().lowercase()
            if (seen.add(key)) {
                deduplicated.add(channel)
            }
        }

        cachedChannels = deduplicated
        return Result.success(deduplicated)
    }

    suspend fun fetchEpg(url: String): Result<Map<String, Programme>> = withContext(Dispatchers.IO) {
        try {
            val client = buildClient()
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
