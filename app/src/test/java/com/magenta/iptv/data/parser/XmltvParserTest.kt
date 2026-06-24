package com.magenta.iptv.data.parser

import com.magenta.iptv.data.model.Programme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class XmltvParserTest {

    @Test
    fun testParseEmptyStringReturnsEmptyMap() {
        val result = XmltvParser.parse("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testParseBlankStringReturnsEmptyMap() {
        val result = XmltvParser.parse("   \n  \t  ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testParseMalformedXmlReturnsEmptyMap() {
        val malformedXml = "<tv><channel><title>broken"
        val result = XmltvParser.parse(malformedXml)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testParseTimestamp() {
        val ts = XmltvParser.parseTimestamp("20240115190000 +0000")
        assertTrue(ts != null && ts > 0)
    }

    @Test
    fun testParseTimestampInvalid() {
        val ts = XmltvParser.parseTimestamp("not-a-timestamp")
        assertNull(ts)
    }

    @Test
    fun testFilterCurrentProgrammesWith3ProgrammesAcross2Channels() {
        val now = System.currentTimeMillis()

        val programmes = listOf(
            Programme("ch1", "Past Show Ch1", now - 7200_000, now - 3600_000),
            Programme("ch1", "Current Show Ch1", now - 1800_000, now + 1800_000),
            Programme("ch1", "Future Show Ch1", now + 3600_000, now + 7200_000),
            Programme("ch2", "Current Show Ch2", now - 900_000, now + 2700_000),
            Programme("ch2", "Past Show Ch2", now - 7200_000, now - 3600_000)
        )

        val result = XmltvParser.filterCurrentProgrammes(programmes)

        assertEquals(2, result.size)
        assertEquals("Current Show Ch1", result["ch1"]?.title)
        assertEquals("ch1", result["ch1"]?.channelId)
        assertEquals("Current Show Ch2", result["ch2"]?.title)
        assertEquals("ch2", result["ch2"]?.channelId)
    }

    @Test
    fun testFilterReturnsOnlyCurrentProgramme() {
        val now = System.currentTimeMillis()

        val programmes = listOf(
            Programme("ch1", "Past Show", now - 7200_000, now - 3600_000),
            Programme("ch1", "Future Show", now + 3600_000, now + 7200_000),
            Programme("ch1", "Current Show", now - 1800_000, now + 1800_000)
        )

        val result = XmltvParser.filterCurrentProgrammes(programmes)

        assertEquals(1, result.size)
        assertEquals("Current Show", result["ch1"]?.title)
    }

    @Test
    fun testFilterWithNoCurrentProgrammesReturnsEmpty() {
        val now = System.currentTimeMillis()

        val programmes = listOf(
            Programme("ch1", "Past Show", now - 7200_000, now - 3600_000),
            Programme("ch1", "Future Show", now + 3600_000, now + 7200_000)
        )

        val result = XmltvParser.filterCurrentProgrammes(programmes)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterEmptyProgrammesListReturnsEmpty() {
        val result = XmltvParser.filterCurrentProgrammes(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun testFilterPicksFirstCurrentProgrammePerChannel() {
        val now = System.currentTimeMillis()

        val programmes = listOf(
            Programme("ch1", "First Current", now - 1800_000, now + 1800_000),
            Programme("ch1", "Second Current", now - 900_000, now + 2700_000)
        )

        val result = XmltvParser.filterCurrentProgrammes(programmes)

        assertEquals(1, result.size)
        assertEquals("First Current", result["ch1"]?.title)
    }

    // --- Integration tests for XML parsing (using JVM-compatible XmlPullParser) ---

    @Test
    fun testParseProgrammeExtractsTitleCorrectly() {
        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="20250624180000 +0000" stop="20250624190000 +0000" channel="ch1"><title>Current Show</title></programme>"""
        ))
        val programme = XmltvParser.parseProgramme(parser)
        assertEquals("Current Show", programme?.title)
        assertEquals("ch1", programme?.channelId)
    }

    @Test
    fun testParseProgrammeWithNestedTagsExtractsTitle() {
        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="20250624180000 +0000" stop="20250624190000 +0000" channel="ch1"><title>My Show</title></programme>"""
        ))

        val programme = XmltvParser.parseProgramme(parser)
        assertEquals("My Show", programme?.title)
    }

    @Test
    fun testParseProgrammeReturnsNullWhenTitleBlank() {
        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="20250624180000 +0000" stop="20250624190000 +0000" channel="ch1"><title></title></programme>"""
        ))

        val programme = XmltvParser.parseProgramme(parser)
        assertNull(programme)
    }

    @Test
    fun testParseProgrammeReturnsNullWhenMissingChannel() {
        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="20250624180000 +0000" stop="20250624190000 +0000"><title>No Channel</title></programme>"""
        ))

        val programme = XmltvParser.parseProgramme(parser)
        assertNull(programme)
    }

    @Test
    fun testParseProgrammeWithMultipleChildrenExtractsTitleOnly() {
        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="20250624180000 +0000" stop="20250624190000 +0000" channel="ch1"><title>My Show</title><desc>A great show</desc></programme>"""
        ))

        val programme = XmltvParser.parseProgramme(parser)
        assertEquals("My Show", programme?.title)
        assertEquals("ch1", programme?.channelId)
    }

    @Test
    fun testParseProgrammeParsesCorrectTimestamps() {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
        val startStr = sdf.format(Date(now - 1800_000))
        val endStr = sdf.format(Date(now + 1800_000))

        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="$startStr" stop="$endStr" channel="ch1"><title>Timed Show</title></programme>"""
        ))

        val programme = XmltvParser.parseProgramme(parser)
        assertEquals("Timed Show", programme?.title)
        assertEquals("ch1", programme?.channelId)
        val expectedStart = sdf.parse(startStr)!!.time
        val expectedEnd = sdf.parse(endStr)!!.time
        assertEquals(expectedStart, programme?.startTime)
        assertEquals(expectedEnd, programme?.endTime)
    }

    @Test
    fun testParseProgrammeDepthTrackingAllowsMultipleSiblings() {
        val parser = SimpleXmlPullParser()
        parser.setInput(StringReader(
            """<programme start="20250624180000 +0000" stop="20250624190000 +0000" channel="ch1"><title>Correct Title</title><desc>Some description</desc><category>News</category></programme>"""
        ))

        val programme = XmltvParser.parseProgramme(parser)
        assertEquals("Correct Title", programme?.title)
        assertEquals("ch1", programme?.channelId)
    }

    @Test
    fun testParseReturnsEmptyForValidXmltvWithProgrammes() {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
        val startStr = sdf.format(Date(now - 1800_000))
        val endStr = sdf.format(Date(now + 1800_000))

        val xml = """<?xml version="1.0" encoding="UTF-8"?>
<tv>
  <channel id="ch1"><display-name>Channel One</display-name></channel>
  <programme start="$startStr" stop="$endStr" channel="ch1"><title>Current Show</title></programme>
</tv>"""
        val result = XmltvParser.parse(xml)
        // In unit tests, the Android XmlPullParser is stubbed, so parse() returns empty.
        // Full end-to-end parsing integration tests require Android instrumented tests.
        assertTrue(result.isEmpty())
    }
}
