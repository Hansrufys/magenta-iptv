package com.magenta.iptv.data.parser

import android.util.Xml
import com.magenta.iptv.data.model.Programme
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object XmltvParser {

    private val timestampFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parse(xmlContent: String): Map<String, Programme> {
        if (xmlContent.isBlank()) return emptyMap()

        val programmes = mutableListOf<Programme>()

        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "programme") {
                    val programme = parseProgramme(parser)
                    if (programme != null) {
                        programmes.add(programme)
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            return emptyMap()
        }

        return filterCurrentProgrammes(programmes)
    }

    internal fun filterCurrentProgrammes(programmes: List<Programme>): Map<String, Programme> {
        val now = System.currentTimeMillis()
        return programmes
            .filter { now >= it.startTime && now < it.endTime }
            .groupBy { it.channelId }
            .mapValues { entry -> entry.value.first() }
    }

    internal fun parseTimestamp(value: String): Long? {
        return try {
            timestampFormat.parse(value)?.time
        } catch (e: Exception) {
            null
        }
    }

    internal fun parseProgramme(parser: XmlPullParser): Programme? {
        return try {
            val startStr = parser.getAttributeValue(null, "start")
            val stopStr = parser.getAttributeValue(null, "stop")
            val channelId = parser.getAttributeValue(null, "channel") ?: return null

            if (startStr == null || stopStr == null) return null

            val startTime = parseTimestamp(startStr) ?: return null
            val endTime = parseTimestamp(stopStr) ?: return null

            var title = ""
            var depth = 1
            while (depth > 0) {
                val eventType = parser.next()
                when (eventType) {
                    XmlPullParser.START_TAG -> depth++
                    XmlPullParser.END_TAG -> depth--
                    XmlPullParser.TEXT -> {
                        if (depth == 2 && parser.name == "title") {
                            title = parser.text?.trim() ?: ""
                        }
                    }
                }
            }

            if (title.isBlank()) return null

            Programme(
                channelId = channelId,
                title = title,
                startTime = startTime,
                endTime = endTime
            )
        } catch (e: Exception) {
            null
        }
    }
}
