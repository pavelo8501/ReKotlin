package po.test.misc.data.pretty_print

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.pretty_print.DynamicNormalizer
import po.misc.data.pretty_print.PrettyCell
import po.misc.data.pretty_print.PrettyPresets
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.TrimNormalizer
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.TextStyle
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class TestPrettyCell {

    @Test
    fun `String distributions inside the row`(){
        val cell1 = PrettyCell(20)
        val cell2 = PrettyCell(30)

        val row = PrettyRow(cell1, cell2)
        val outStr1 = "String outside the row"
        val outStr2 = "Second string outside the row"
        val inputList = listOf("String_1", "String_2", outStr1, outStr2)
        val result =  row.render(inputList)
        assertTrue {
            result.contains(outStr1) &&
                    result.contains(outStr2)
        }
        result.output()
    }

    @Test
    fun `Styling preset usage`(){
        val keyCell = PrettyCell(20, PrettyPresets.Key)
        val rendered = keyCell.render("Some Key")
        assertTrue { rendered.contains(TextStyle.ITALIC.code) }
        assertTrue { rendered.contains(Colour.Gray.code) }
        assertTrue { rendered.contains(SpecialChars.RIGHT_SEMICOLON) }
        rendered.output()

        val valueCell = PrettyCell(20, PrettyPresets.Value)
        val valueCellRendered = valueCell.render("Some Value")
        assertFalse{ valueCellRendered.contains(TextStyle.ITALIC.code) }
        assertTrue { valueCellRendered.contains(Colour.CyanBright.code) }
        valueCellRendered.output()
    }

    @Test
    fun `String normalization work as expected`(){

        val maxSize = 5
        val sizeNormalization = DynamicNormalizer(
            SpecialChars.DOTS,
            { it.length >= maxSize },
            { it.substring(0, maxSize) }
        )
        val keyCell = PrettyCell(maxSize, PrettyPresets.Value)
        keyCell.addTextNormalizer(sizeNormalization)
        val initialString = "Some text that is definitely has more than 5 chars"
        val rendered = keyCell.render(initialString)

        assertTrue { rendered.length < initialString.length }
        assertTrue { rendered.contains("...") }
        rendered.output()
    }

    @Test
    fun `Pere built normalizers produce same result`(){

        val maxSize = 5
        val sizeNormalization = DynamicNormalizer(
            SpecialChars.DOTS,
            { it.length >= maxSize },
            { it.substring(0, maxSize) }
        )
        val keyCell = PrettyCell(maxSize, PrettyPresets.Value)
        keyCell.addTextNormalizer(sizeNormalization)
        val initialString = "Some text that is definitely has more than 5 chars"
        val rendered = keyCell.render(initialString)
        keyCell.resetNormalizers()
        keyCell.addTextNormalizer(TrimNormalizer(maxSize, SpecialChars.DOTS))
        val rendered2 = keyCell.render(initialString)
        assertEquals(rendered, rendered2)
        rendered2.output()
    }

}