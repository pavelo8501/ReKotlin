package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.KeyedCellOptions
import po.misc.data.pretty_print.formatters.text_modifiers.ColorModifier
import po.misc.data.pretty_print.formatters.text_modifiers.TextTrimmer
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestKeyedCell {

    class KeyedRecord (
        val text: String,
        val result: PrettyCellResult = PrettyCellResult.AccessPretty
    ): Templated{
        enum class PrettyCellResult { AccessPretty, ReadPretty }
    }

    @Test
    fun `Pere built colour modifier overrides default behaviour`(){
        val record = KeyedRecord("Text", result = KeyedRecord.PrettyCellResult.AccessPretty)
        val condition1 = ColorModifier.ColourCondition(KeyedRecord.PrettyCellResult.AccessPretty.name, Colour.YellowBright)
        val condition2 = ColorModifier.ColourCondition(KeyedRecord.PrettyCellResult.ReadPretty.name, Colour.Blue)
        val prettyRow = record.buildPrettyRow {
           // addCell(record::text)
           // addCell(record::result, ColorModifier(condition1, condition2))
        }
        val render1 = prettyRow.render(record)
        assertTrue { render1.contains(Colour.YellowBright.code) }

        val record2 = KeyedRecord("Text2", result = KeyedRecord.PrettyCellResult.ReadPretty)
        val render2 = prettyRow.render(record2)
        assertTrue { render2.contains(Colour.Blue.code) }
    }

    @Test
    fun `Pere built modifiers produce correct result when used together`() {
        val record = KeyedRecord("text", result = KeyedRecord.PrettyCellResult.AccessPretty)
        val condition1 = ColorModifier.ColourCondition("Access...", Colour.YellowBright)
        val condition2 = ColorModifier.ColourCondition("Read", Colour.Blue)
        val prettyRow = record.buildPrettyRow {
          //  addCell(record::text)
          //  addCell(record::result, ColorModifier(condition1, condition2), TextTrimmer(maxLength = 6, "..."))
        }
        val render = prettyRow.render(record)
        assertTrue { render.contains("Access...") && !render.contains("Pretty") }
        val record2 = KeyedRecord("text_2", result = KeyedRecord.PrettyCellResult.ReadPretty)
        val prettyRow2 = record.buildPrettyRow {
          //  addCell(record::text)
          //  addCell(record::result, ColorModifier(condition1, condition2), TextTrimmer(maxLength = 6, "..."))
        }
        val render2 = prettyRow2.render(record2)
        assertTrue { render2.contains("Read") && !render2.contains("Pretty") }
    }


    @Test
    fun `Options work as expected`() {
        val record = KeyedRecord("No key value", result = KeyedRecord.PrettyCellResult.AccessPretty)
        val condition1 = ColorModifier.ColourCondition("Access...", Colour.YellowBright)
        val condition2 = ColorModifier.ColourCondition("Read", Colour.Blue)
        val option = KeyedCellOptions(showKey = false)
        val prettyRow = record.buildPrettyRow {
          //  addCell(record::text, option)
          //  addCell(record::result, ColorModifier(condition1, condition2), TextTrimmer(maxLength = 6, "..."))
        }
        val render = prettyRow.render(record)
        assertFalse { render.contains("text") }
    }
}