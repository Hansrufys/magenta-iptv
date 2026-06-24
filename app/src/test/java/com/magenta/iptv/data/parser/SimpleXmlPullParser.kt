package com.magenta.iptv.data.parser

import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.Reader
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

class SimpleXmlPullParser : XmlPullParser {

    private sealed class Event {
        data class StartTag(val name: String, val attributes: Map<String, String>) : Event()
        data class EndTag(val name: String) : Event()
        data class TextData(val text: String, val parentName: String?) : Event()
    }

    private var events = mutableListOf<Event>()
    private var currentIndex = 0

    override fun setInput(input: Reader?) {
        events.clear()
        currentIndex = 0
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = false
        val saxParser = factory.newSAXParser()
        val xmlReader = saxParser.xmlReader

        val elementStack = mutableListOf<String>()

        xmlReader.contentHandler = object : DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                val name = qName ?: localName ?: ""
                elementStack.add(name)
                val attrs = mutableMapOf<String, String>()
                if (attributes != null) {
                    for (i in 0 until attributes.length) {
                        attrs[attributes.getQName(i)] = attributes.getValue(i)
                    }
                }
                events.add(Event.StartTag(name, attrs))
            }
            override fun endElement(uri: String?, localName: String?, qName: String?) {
                val name = qName ?: localName ?: ""
                events.add(Event.EndTag(name))
                if (elementStack.isNotEmpty()) elementStack.removeAt(elementStack.lastIndex)
            }
            override fun characters(ch: CharArray?, start: Int, length: Int) {
                if (ch != null && length > 0) {
                    val text = String(ch, start, length)
                    if (text.isNotBlank()) {
                        val parent = elementStack.lastOrNull()
                        events.add(Event.TextData(text, parent))
                    }
                }
            }
        }
        xmlReader.parse(InputSource(input))
    }

    override fun setInput(inputStream: InputStream?, inputEncoding: String?) {}

    override fun next(): Int {
        currentIndex++
        return eventType
    }

    override fun getEventType(): Int {
        if (currentIndex >= events.size) return XmlPullParser.END_DOCUMENT
        return when (events[currentIndex]) {
            is Event.StartTag -> XmlPullParser.START_TAG
            is Event.EndTag -> XmlPullParser.END_TAG
            is Event.TextData -> XmlPullParser.TEXT
        }
    }

    override fun getName(): String? {
        if (currentIndex >= events.size) return null
        return when (val event = events[currentIndex]) {
            is Event.StartTag -> event.name
            is Event.EndTag -> event.name
            is Event.TextData -> event.parentName
        }
    }

    override fun getAttributeValue(namespace: String?, name: String?): String? {
        if (currentIndex >= events.size) return null
        val event = events[currentIndex]
        if (event is Event.StartTag) return event.attributes[name]
        return null
    }

    override fun getText(): String? {
        if (currentIndex >= events.size) return null
        val event = events[currentIndex]
        if (event is Event.TextData) return event.text
        return null
    }

    override fun setFeature(name: String?, state: Boolean) {}
    override fun getFeature(name: String?): Boolean = false
    override fun setProperty(name: String?, value: Any?) {}
    override fun getProperty(name: String?): Any? = null
    override fun getInputEncoding(): String = "UTF-8"
    override fun defineEntityReplacementText(entityName: String?, replacementText: String?) {}
    override fun getNamespaceCount(depth: Int): Int = 0
    override fun getNamespacePrefix(pos: Int): String? = null
    override fun getNamespaceUri(pos: Int): String? = null
    override fun getNamespace(prefix: String?): String? = null
    override fun getDepth(): Int = 0
    override fun getPositionDescription(): String = ""
    override fun getLineNumber(): Int = 0
    override fun getColumnNumber(): Int = 0
    override fun isWhitespace(): Boolean = false
    override fun getTextCharacters(holderForStartAndLength: IntArray?): CharArray? = null
    override fun isEmptyElementTag(): Boolean = false
    override fun getAttributeCount(): Int {
        if (currentIndex >= events.size) return 0
        val event = events[currentIndex]
        if (event is Event.StartTag) return event.attributes.size
        return 0
    }
    override fun getAttributeNamespace(index: Int): String? = null
    override fun getAttributeName(index: Int): String? {
        if (currentIndex >= events.size) return null
        val event = events[currentIndex]
        if (event is Event.StartTag) return event.attributes.keys.elementAtOrNull(index)
        return null
    }
    override fun getAttributePrefix(index: Int): String? = null
    override fun getAttributeType(index: Int): String = "CDATA"
    override fun isAttributeDefault(index: Int): Boolean = false
    override fun getAttributeValue(index: Int): String? {
        if (currentIndex >= events.size) return null
        val event = events[currentIndex]
        if (event is Event.StartTag) return event.attributes.values.elementAtOrNull(index)
        return null
    }
    override fun nextToken(): Int = next()
    override fun nextTag(): Int {
        var type = next()
        while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
            type = next()
        }
        return type
    }
    override fun nextText(): String {
        if (eventType == XmlPullParser.TEXT) {
            val text = getText() ?: ""
            next()
            return text
        }
        return ""
    }
    override fun require(type: Int, namespace: String?, name: String?) {}
    override fun getPrefix(): String? = null
    override fun getNamespace(): String? = null
}
