package po.test.misc.data.pretty_print.parts

import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.toProvider
import po.misc.data.styles.Colour
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestOptions : PrettyTestBase(){

   private val record = PrintableRecord()

    @Test
    fun `Cells rendered correctly with presets applied`(){

        PrintableRecord::name.toProvider()

        val keyedCell = KeyedCell(PrintableRecord::name.toProvider(), CellPresets.KeylessProperty)
        val render = keyedCell.render(record)
        assertTrue  { render.contains(Colour.GreenBright.code) }
        assertFalse { render.contains("Name") }
    }

    @Test
    fun `Cells rendered correctly with options applied `(){

        val options = Options(CellPresets.Property)
        options.renderKey = false
        val keyedCell = KeyedCell(PrintableRecord::description)
        assertFalse { keyedCell.cellOptions.renderKey }
        var render = keyedCell.render(record)
        assertTrue  { render.contains(record.description) }
        assertFalse { render.contains("Description") }

        options.renderKey = true
        options.plainKey = true
        keyedCell.applyOptions(options)
        render = keyedCell.render(record)
        assertTrue{ render.contains("Description") }
        assertFalse{ render.contains(Colour.Magenta.code) }
        assertTrue{ render.contains(Colour.GreenBright.code) }

        options.plainKey = false
        options.plainText = true
        keyedCell.applyOptions(options)
        render = keyedCell.render(record)
        assertTrue{ render.contains("Description") }
        assertTrue{ render.contains(Colour.Magenta.code) }
        assertFalse { render.contains(Colour.GreenBright.code) }
    }
}