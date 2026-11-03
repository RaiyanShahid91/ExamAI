package com.ai.studyassistant.i18n

import org.junit.Assert.*
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/** Checks the actual shipped XML, including strings not exercised by individual UI tests. */
class TranslationResourcesTest {
    private val res = File("src/main/res").let { if (it.isDirectory) it else File("app/src/main/res") }
    private val tags = listOf("hi", "bn", "mr", "te", "ta", "ur", "kn", "ml", "gu")
    private val token = Regex("%\\d+\\$[sd]|%%")

    private fun resources(file: File): Map<String, Element> {
        val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement
        val elements = (0 until root.childNodes.length).mapNotNull { root.childNodes.item(it) as? Element }
        assertEquals("Duplicate resource in $file", elements.size, elements.map { it.getAttribute("name") }.distinct().size)
        return elements.associateBy { it.getAttribute("name") }
    }

    @Test fun everySupportedLanguageHasEveryTranslatableResourceAndMatchingFormats() {
        val source = resources(File(res, "values/strings.xml"))
            .filterValues { it.getAttribute("translatable") != "false" }
        for (tag in tags) {
            val translated = resources(File(res, "values-$tag/strings.xml"))
            assertEquals("Resource coverage for $tag", source.keys, translated.keys)
            for ((name, original) in source) {
                val target = translated.getValue(name)
                assertEquals("Resource type for $tag/$name", original.tagName, target.tagName)
                assertEquals("Format flag for $tag/$name", original.getAttribute("formatted"), target.getAttribute("formatted"))
                if (original.tagName == "plurals") {
                    val originals = original.getElementsByTagName("item")
                    val items = target.getElementsByTagName("item")
                    val quantities = (0 until items.length).map { (items.item(it) as Element).getAttribute("quantity") }
                    assertTrue("Missing other plural for $tag/$name", "other" in quantities)
                    for (i in 0 until items.length) {
                        val item = items.item(i) as Element
                        val quantity = item.getAttribute("quantity")
                        val matching = (0 until originals.length).map { originals.item(it) as Element }
                            .firstOrNull { it.getAttribute("quantity") == quantity }
                            ?: (0 until originals.length).map { originals.item(it) as Element }.first { it.getAttribute("quantity") == "other" }
                        checkText("$tag/$name/$quantity", matching, item, tag)
                    }
                } else checkText("$tag/$name", original, target, tag)
            }
        }
    }

    private fun checkText(label: String, original: Element, translated: Element, tag: String) {
        val text = translated.textContent
        assertTrue("Empty $label", text.isNotBlank())
        assertFalse("Unrestored placeholder in $label", Regex("ZXQ\\d+XQZ").containsMatchIn(text))
        val expected = token.findAll(original.textContent).map { it.value }.sorted().toList()
        assertEquals("Placeholders for $label", expected, token.findAll(text).map { it.value }.sorted().toList())
        if (original.getAttribute("formatted") != "false") {
            val values = Regex("%(\\d+)\\$([sd])").findAll(text).associate { it.groupValues[1].toInt() to it.groupValues[2] }
            val args = (1..(values.keys.maxOrNull() ?: 0)).map { if (values[it] == "d") 7 else "JEE" }.toTypedArray()
            String.format(Locale.forLanguageTag(tag), text, *args)
        }
    }

    @Test fun languagePickerAndSystemConfigMatchAvailableTranslations() {
        val config = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(res, "xml/locales_config.xml"))
        val locales = config.getElementsByTagName("locale")
        val configured = (0 until locales.length).map { (locales.item(it) as Element).getAttribute("android:name") }.toSet()
        assertEquals((tags + "en").toSet(), configured)
        assertEquals(configured, LanguageManager.supported.map { it.tag }.toSet())
    }
}
