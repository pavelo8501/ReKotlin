package po.test.misc.data.pretty_print.cells

import org.junit.jupiter.api.Test
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.rows.buildPrettyRow
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestKeyedCell: PrettyTestBase() {

    @Test
    fun `Keyed cell render with defaults`(){
        val row = buildPrettyRow<PrintableRecord> {
            add(PrintableRecord::name)
        }
        val cell = assertIs<KeyedCell<PrintableRecord>>(row.cells.first())
        assertEquals(TextStyle.Italic, cell.keyStyle.textStyle)
        assertEquals(Colour.Magenta, cell.keyStyle.colour)
        assertEquals(Colour.GreenBright, cell.valueStyle.colour)
        assertNotNull(cell.row)

        val render = cell.render(createRecord())
        assertTrue { render.contains(TextStyle.Italic.code)  }
        assertTrue { render.contains(Colour.Magenta.code)  }
        assertTrue { render.contains(Colour.GreenBright.code)  }
    }

    @Test
    fun `Keyed cell render options can be overwritten by presets`(){
        val opt = CellPresets.Property.asOptions()
        opt.keyStyle.colour = null
        val row = buildPrettyRow<PrintableRecord> {
            add(PrintableRecord::name, opt)
        }
        val cell = assertIs<KeyedCell<PrintableRecord>>(row.cells.first())
        assertEquals(TextStyle.Italic, cell.keyStyle.textStyle)
        assertEquals(null, cell.keyStyle.colour)
        assertEquals(Colour.GreenBright, cell.valueStyle.colour)

        val render = cell.render(createRecord())
        assertTrue { render.contains(TextStyle.Italic.code)  }
        assertFalse { render.contains(Colour.Magenta.code)  }
        assertTrue { render.contains(Colour.GreenBright.code)  }
    }

}