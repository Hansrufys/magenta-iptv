package com.magenta.iptv.data.parser

import com.magenta.iptv.data.model.Channel

object M3uParser {

    private val TVG_ID_PATTERN = Regex("""tvg-id="([^"]*)"""")
    private val TVG_NAME_PATTERN = Regex("""tvg-name="([^"]*)"""")
    private val TVG_LOGO_PATTERN = Regex("""tvg-logo="([^"]*)"""")
    private val GROUP_TITLE_PATTERN = Regex("""group-title="([^"]*)"""")

    fun parse(m3uContent: String): List<Channel> {
        if (m3uContent.isBlank()) return emptyList()

        val lines = m3uContent.lines()
        val channels = mutableListOf<Channel>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.startsWith("#EXTINF:")) {
                val extinfContent = line.removePrefix("#EXTINF:")
                val result = parseExtinf(extinfContent, lines, i)

                if (result != null) {
                    channels.add(result.first)
                    i = result.second
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        return channels
    }

    /**
     * Parse a single EXTINF block.
     * @return Pair of (Channel, nextLineIndex) or null if entry is invalid.
     */
    private fun parseExtinf(
        extinfContent: String,
        lines: List<String>,
        lineIndex: Int
    ): Pair<Channel, Int>? {
        val epgId = TVG_ID_PATTERN.find(extinfContent)?.groupValues?.get(1)
        val tvgName = TVG_NAME_PATTERN.find(extinfContent)?.groupValues?.get(1)
        val logoUrl = TVG_LOGO_PATTERN.find(extinfContent)?.groupValues?.get(1)
        val groupTitle = GROUP_TITLE_PATTERN.find(extinfContent)?.groupValues?.get(1)

        // Channel name is the text after the last comma
        val lastCommaIndex = extinfContent.lastIndexOf(',')
        val nameFromExtinf = if (lastCommaIndex >= 0) {
            extinfContent.substring(lastCommaIndex + 1).trim()
        } else {
            ""
        }

        val name = if (!tvgName.isNullOrBlank()) tvgName else nameFromExtinf
        if (name.isBlank()) return null

        var nextIndex = lineIndex + 1
        while (nextIndex < lines.size) {
            val candidate = lines[nextIndex].trim()
            if (candidate.isBlank()) {
                nextIndex++
                continue
            }
            if (candidate.startsWith("#")) {
                break
            }
            val id = if (!epgId.isNullOrBlank()) {
                epgId
            } else {
                name.lowercase().replace(" ", "_")
            }
            return Pair(
                Channel(
                    id = id,
                    name = name,
                    logoUrl = logoUrl,
                    streamUrl = candidate,
                    groupTitle = groupTitle,
                    epgId = epgId
                ),
                nextIndex + 1
            )
        }

        // No stream URL found — skip this entry
        return null
    }
}
