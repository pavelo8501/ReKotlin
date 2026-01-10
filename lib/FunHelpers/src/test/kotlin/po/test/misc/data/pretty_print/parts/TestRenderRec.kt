package po.test.misc.data.pretty_print.parts

import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.strings.FormattedText
import po.misc.data.strings.createFormatted
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRenderRec {

    private val text = "Some text"

    @Test
    fun `Init  logic`() {
        val options = Options()
        val colorizedText = text.colorize(Colour.Blue)
        val key = "Text"
        val colorizedKey = key.colorize(Colour.CyanBright)
        val totalSize = key.length + text.length + options.keySeparator.displaySize
        val formatted = FormattedText(text, colorizedText)
        val renderRec = RenderRecord(formatted)

        assertEquals(text, renderRec.plain)
        assertEquals(colorizedText, renderRec.formatted)

        val keyPart = PrettyCellBase.KeyValueTags.Key.createFormatted(key, colorizedKey)
        renderRec.addKey(keyPart, options.keySeparator)

        assertEquals("$key:$text", renderRec.plain, "Plain text does not match")
        assertEquals("$colorizedKey:$colorizedText", renderRec.formatted, "Formatted text does not match")

        assertEquals(key, renderRec.plainKey, "Plain key changed")
        assertEquals(colorizedKey, renderRec.formattedKey, "Formatted key changed")
        assertEquals(text, renderRec.plainValue, "Plain value changed")
        assertEquals(colorizedText, renderRec.formattedValue, "Formatted value changed")

        assertEquals(key.length, renderRec.plainKeySize, "Plain key size mismatch")
        assertEquals(text.length, renderRec.plainValueSize, "Plain value size mismatch")

        assertEquals(totalSize, renderRec.totalPlainLength, "TotalPlainLength, mismatch")
    }

    @Test
    fun `Update value`() {
        val options = Options()
        val colorizedText = text.colorize(Colour.Blue)
        val key = "Text"
        val colorizedKey = key.colorize(Colour.CyanBright)
        val formatted = FormattedText(text, colorizedText)
        val renderRec = RenderRecord(formatted)
        renderRec.addKey(PrettyCellBase.KeyValueTags.Key.createFormatted(key, colorizedKey), options.keySeparator)
        val newValue = "Some"
        val newColorized = "Some".colorize(Colour.Yellow)
        renderRec.setValue(newValue, newColorized)

        assertEquals(newValue, renderRec.plainValue)
        assertEquals(newColorized, renderRec.formattedValue)
        assertEquals("$key:$newValue", renderRec.plain)
    }

    @Test
    fun `Update key`() {
        val options = Options()
        val colorizedText = text.colorize(Colour.Blue)
        val key = "Text"
        val colorizedKey = key.colorize(Colour.CyanBright)
        val formatted = FormattedText(text, colorizedText)
        val renderRec = RenderRecord(formatted)
        renderRec.addKey(PrettyCellBase.KeyValueTags.Key.createFormatted(key, colorizedKey), options.keySeparator)
        val newKey = "Text 2"
        val newColorizedKey = newKey.colorize(Colour.Yellow)
        renderRec.setKey(newKey, newColorizedKey)

        assertEquals(newKey, renderRec.plainKey)
        assertEquals(newColorizedKey, renderRec.formattedKey)
        assertEquals("$newKey:$text", renderRec.plain)
        assertEquals("$newColorizedKey:$colorizedText", renderRec.formatted)
    }

}