package po.test.misc.data.pretty_print.parts

import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.CellPresets
import po.misc.data.pretty_print.parts.Options
import po.misc.data.styles.Colour
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestParts : PrettyTestBase(){

   private val record = PrintableRecord()

    @Test
    fun `Cells rendered correctly with presets applied`(){
        val keyedCell = KeyedCell(tokenOf<PrintableRecord>(), PrintableRecord::name).applyOptions(CellPresets.KeylessProperty)
        val render = keyedCell.render(record)
        assertTrue  { render.contains(Colour.GreenBright.code) }
        assertFalse { render.contains("Name") }
    }

    @Test
    fun `Cells rendered correctly with options applied `(){

        val options = Options(CellPresets.Property)
        options.renderKey = false
        val keyedCell = KeyedCell(tokenOf<PrintableRecord>(), PrintableRecord::description).applyOptions(options)
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