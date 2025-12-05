package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.formatters.text_modifiers.ColorModifier
import po.misc.data.pretty_print.formatters.text_modifiers.TextTrimmer
import po.misc.data.pretty_print.presets.PrettyPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyle
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestPrettyCell: Component {

    class Record (
        val text: String,
        val result: CellResult = CellResult.AccessPretty
    ){
        enum class CellResult { AccessPretty, ReadPretty }
    }

    @Test
    fun `String distributions inside the row`(){
        val cell1 = PrettyCell(20)
        val cell2 = PrettyCell(30)
        val row = PrettyRow(cell1, cell2)
        val outStr1 = "String outside the row"
        val outStr2 = "Second string outside the row"
        val inputList = listOf("String_1", "String_2", outStr1, outStr2)
        val result =  row.render(inputList)
        assertTrue { result.contains(outStr1) && result.contains(outStr2) }
        result.output()
    }


    fun `Styling preset usage`(){
        val cell = PrettyCell(20).applyPreset(PrettyPresets.Key)
        val rendered = cell.render("Some Key")
        assertTrue { rendered.contains(TextStyle.Italic.code) }
        assertTrue { rendered.contains(Colour.Gray.code) }
        assertTrue { rendered.contains(SpecialChars.RIGHT_SEMICOLON) }
        val valueCell = PrettyCell(20).applyPreset(PrettyPresets.Value)
        val valueCellRendered = valueCell.render("Some Value")
        assertFalse { valueCellRendered.contains(TextStyle.Italic.code) }
        assertTrue { valueCellRendered.contains(Colour.CyanBright.code) }
    }


    fun `Pre built text modifier overrides default behaviour`(){
        val textCell = PrettyCell(width = 20).applyPreset(PrettyPresets.Key)
        val condition1 = ColorModifier.ColourCondition("Long", Colour.Red)
        val condition2 = ColorModifier.ColourCondition("Short", Colour.Blue)
        textCell.addModifiers(TextTrimmer(4, "..."), ColorModifier(condition1, condition2))
        val cellResult = PrettyCell(30)
        val row = PrettyRow(textCell, cellResult)
       // val rendered = row.render("Long text to be trimmed", Record.CellResult.AccessPretty)
        //assertTrue { rendered.contains("Long...") && rendered.contains(Colour.Red.code) }
    }

}