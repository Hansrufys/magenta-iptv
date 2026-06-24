package com.magenta.iptv.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    private val sampleM3u = """
        #EXTM3U
        #EXTINF:-1 tvg-id="ch1" tvg-name="Channel One" tvg-logo="https://example.com/logo1.png" group-title="News",Channel One
        https://stream.example.com/ch1.m3u8
        #EXTINF:-1 tvg-id="ch2" tvg-name="Channel Two" tvg-logo="https://example.com/logo2.png" group-title="News",Channel Two
        https://stream.example.com/ch2.m3u8
        #EXTINF:-1 tvg-id="ch3" tvg-name="Channel Three" group-title="Sports",Channel Three
        https://stream.example.com/ch3.m3u8
    """.trimIndent()

    @Test
    fun testParseValidM3u() {
        val channels = M3uParser.parse(sampleM3u)

        assertEquals(3, channels.size)

        // Channel 1
        assertEquals("ch1", channels[0].id)
        assertEquals("Channel One", channels[0].name)
        assertEquals("https://example.com/logo1.png", channels[0].logoUrl)
        assertEquals("https://stream.example.com/ch1.m3u8", channels[0].streamUrl)
        assertEquals("News", channels[0].groupTitle)
        assertEquals("ch1", channels[0].epgId)

        // Channel 2
        assertEquals("ch2", channels[1].id)
        assertEquals("Channel Two", channels[1].name)
        assertEquals("https://example.com/logo2.png", channels[1].logoUrl)
        assertEquals("https://stream.example.com/ch2.m3u8", channels[1].streamUrl)
        assertEquals("News", channels[1].groupTitle)
        assertEquals("ch2", channels[1].epgId)

        // Channel 3
        assertEquals("ch3", channels[2].id)
        assertEquals("Channel Three", channels[2].name)
        assertNull(channels[2].logoUrl)
        assertEquals("https://stream.example.com/ch3.m3u8", channels[2].streamUrl)
        assertEquals("Sports", channels[2].groupTitle)
        assertEquals("ch3", channels[2].epgId)
    }

    @Test
    fun testParseEmptyInput() {
        val channels = M3uParser.parse("")
        assertTrue(channels.isEmpty())
    }

    @Test
    fun testParseMalformedM3u() {
        val malformedM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="ch1" tvg-name="Working Channel" group-title="News",Working Channel
            https://stream.example.com/ch1.m3u8
            #EXTINF:-1 tvg-id="ch2" tvg-name="No Stream"
            #EXTINF:-1 tvg-id="ch3" tvg-name="Another Working" group-title="Sports",Another Working
            https://stream.example.com/ch3.m3u8
        """.trimIndent()

        val channels = M3uParser.parse(malformedM3u)

        assertEquals(2, channels.size)
        assertEquals("ch1", channels[0].id)
        assertEquals("Working Channel", channels[0].name)
        assertEquals("ch3", channels[1].id)
        assertEquals("Another Working", channels[1].name)
    }

    @Test
    fun testParseMissingAttributes() {
        val minimalM3u = """
            #EXTM3U
            #EXTINF:-1,Minimal Channel
            https://stream.example.com/minimal.m3u8
        """.trimIndent()

        val channels = M3uParser.parse(minimalM3u)

        assertEquals(1, channels.size)
        assertEquals("minimal_channel", channels[0].id)
        assertEquals("Minimal Channel", channels[0].name)
        assertEquals("https://stream.example.com/minimal.m3u8", channels[0].streamUrl)
        assertNull(channels[0].logoUrl)
        assertNull(channels[0].groupTitle)
        assertNull(channels[0].epgId)
    }

    @Test
    fun testParseMultipleGroups() {
        val groupM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="ch1" group-title="News",News Channel
            https://stream.example.com/ch1.m3u8
            #EXTINF:-1 tvg-id="ch2" group-title="Sports",Sports Channel
            https://stream.example.com/ch2.m3u8
            #EXTINF:-1 tvg-id="ch3" group-title="Entertainment",Ent Channel
            https://stream.example.com/ch3.m3u8
            #EXTINF:-1 tvg-id="ch4" group-title="News",News Two
            https://stream.example.com/ch4.m3u8
        """.trimIndent()

        val channels = M3uParser.parse(groupM3u)

        assertEquals(4, channels.size)
        assertEquals("News", channels[0].groupTitle)
        assertEquals("Sports", channels[1].groupTitle)
        assertEquals("Entertainment", channels[2].groupTitle)
        assertEquals("News", channels[3].groupTitle)
    }
}
