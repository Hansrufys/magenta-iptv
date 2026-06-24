package com.magenta.iptv.data

object PlaylistUrls {

    data class PlaylistSource(
        val name: String,
        val url: String,
        val category: String
    )

    // Primary Arabic channels playlist (300+ channels)
    private const val IPTV_ORG_ARABIC = "https://iptv-org.github.io/iptv/languages/ara.m3u"

    // Country-specific playlists
    private const val IPTV_ORG_SAUDI = "https://iptv-org.github.io/iptv/countries/sa.m3u"
    private const val IPTV_ORG_UAE = "https://iptv-org.github.io/iptv/countries/ae.m3u"
    private const val IPTV_ORG_EGYPT = "https://iptv-org.github.io/iptv/countries/eg.m3u"
    private const val IPTV_ORG_QATAR = "https://iptv-org.github.io/iptv/countries/qa.m3u"

    // Global playlist (12000+ channels — includes Arabic channels)
    private const val IPTV_ORG_GLOBAL = "https://iptv-org.github.io/iptv/index.m3u"

    val defaultSources = listOf(
        PlaylistSource("Arabic Channels", IPTV_ORG_ARABIC, "Arabic"),
        PlaylistSource("Saudi Arabia", IPTV_ORG_SAUDI, "Arabic"),
        PlaylistSource("UAE", IPTV_ORG_UAE, "Arabic"),
        PlaylistSource("Egypt", IPTV_ORG_EGYPT, "Arabic"),
        PlaylistSource("Qatar", IPTV_ORG_QATAR, "Arabic")
    )

    // Single merged URL for simple fetch (iptv-org supports multiple URLs via comma separation in some clients,
    // but we'll fetch each separately and merge in the repository)
    val primaryUrl: String get() = IPTV_ORG_ARABIC

    val allDefaultUrls: List<String> get() = defaultSources.map { it.url }
}
