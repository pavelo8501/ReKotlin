package po.test.misc.data.pretty_print.cells

import po.misc.data.PrettyPrint
import po.misc.data.logging.procedural.ProceduralEntry
import po.misc.data.output.output
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.options.CellPresets
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.data.styles.contains
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestCellsRendering {

    var parameter: String = "Some text"
    private class FormattedClass(var text:String = "Some text", val useColour:Colour = RedBright): PrettyPrint{
        override val formattedString: String get() = text.colorize(useColour)
    }
    private class PlainClass(var text:String = "Other text", ){
        override fun toString(): String= text
    }

    private val formatted = FormattedClass()
    private val plain = PlainClass("Other text")

    @Test
    fun `Keyed cells render correctly by options provided`(){
        val cell = KeyedCell<TestCellsRendering>(this, ::parameter)
        var render = cell.render(this)
        assertTrue { render.contains(parameter) }
        assertTrue { render.contains(CellPresets.Property.keyStyle.colour) }
        assertTrue { render.contains(CellPresets.Property.style.colour) }

        cell.applyOptions(CellPresets.KeylessProperty)
        render = cell.render(this)
        assertTrue { render.contains(parameter) }
        assertFalse { render.contains(::parameter.displayName) }

        var options = Options()
        options.plainKey = true
        cell.applyOptions(options)
        render = cell.render(this)
        assertTrue { render.contains(::parameter.displayName) }
        assertFalse { render.contains(CellPresets.Property.keyStyle.colour) }

        options = Options()
        options.plainText = true
        cell.applyOptions(options)
        render = cell.render(this)
        assertTrue { render.contains(parameter) }
        assertFalse { render.contains(CellPresets.Property.style.colour) }
    }

    @Test
    fun `Keyed renders text correctly`(){
        var cell = KeyedCell(TestCellsRendering::formatted)
        var render = cell.render(this)
        assertTrue { render.contains(TestCellsRendering::formatted.displayName) }
        assertTrue { render.contains(CellPresets.Property.keyStyle.colour) }
        assertTrue { render.contains(formatted.formattedString) }
        assertFalse { render.contains(CellPresets.Property.style.colour) }

        var options = Options(CellPresets.Property)
        options.useSourceFormatting = true
        cell.applyOptions(options)
        render = cell.render(this)
        assertTrue { render.contains(TestCellsRendering::formatted.displayName) }
        assertTrue { render.contains(CellPresets.Property.keyStyle.colour) }
        assertTrue { render.contains(formatted.text) }
        assertTrue { render.contains(formatted.useColour) }

        options = Options()
        options.plainKey = true
        options.useSourceFormatting = true
        cell.applyOptions(options)
        render = cell.render(this)
        assertTrue { render.contains(TestCellsRendering::formatted.displayName) }
        assertTrue { render.contains(formatted.text) }
        assertTrue { render.contains(formatted.useColour) }
        assertFalse { render.contains(CellPresets.Property.keyStyle.colour) }

        cell = KeyedCell<TestCellsRendering>(this, ::plain)
        render = cell.render(this)
        assertTrue { render.contains(plain.toString()) }
        assertTrue { render.contains(CellPresets.Property.style.colour) }

    }

    @Test
    fun `Keyed cells rendered correctly inside the row`(){
        val cell1 = KeyedCell(TestCellsRendering::formatted)
        val cell2 = KeyedCell(TestCellsRendering::plain)
        val row = PrettyRow(cell1, cell2)
        var render = row.render(this)
        var split = render.lines()

        assertEquals(1, split.size)
        assertTrue { render.contains(plain.toString()) }
        assertTrue { render.contains(formatted.formattedString) }
        val rowOpt = RowOptions(Orientation.Vertical)
        row.applyOptions(rowOpt)
        render = row.render(this)
        split = render.lines()
        assertEquals(2, split.size)
        assertFalse { render.contains(rowOpt.borderSeparator) }
    }

    @Test
    fun `Computed cells render null if no property no result from lambda`(){
        val cell = ComputedCell(TypeToken<TestCellsRendering>(), TypeToken<FormattedClass>()){}
        val render = cell.render(this)
        assertTrue{ render.contains("null")}
    }

    @Test
    fun `Computed cells render default value received by property provided`(){
        val cell = ComputedCell(TestCellsRendering::formatted){}
        var render = cell.render(this)
        assertFalse("Key rendered") { render.contains(TestCellsRendering::formatted.displayName) }
        assertTrue("formattedString not rendered") { render.contains(formatted.formattedString) }
        val  options = Options()
        options.renderKey = true
        cell.applyOptions(options)
        render = cell.render(this)
        render.output()
        assertTrue("formattedString not rendered") { render.contains(formatted.formattedString) }
        assertTrue("Key not rendered") { render.contains(TestCellsRendering::formatted.displayName) }
    }

    @Test
    fun `Computed cells renders value from lambda`(){
        val extraText = "ExtraText"
        val cell = ComputedCell(TestCellsRendering::formatted){
            "${it.formattedString}_$extraText"
        }
        var render = cell.render(this)
        assertTrue("formattedString not rendered") { render.contains(formatted.formattedString) }
        assertTrue("formattedString not rendered") { render.contains(extraText) }

        val cell2 = ComputedCell(TestCellsRendering::formatted){ it }
        render = cell2.render(this)
        assertTrue("formattedString not rendered") { render.contains(formatted.formattedString) }
    }

    @Test
    fun `Computed cell builder works as expected`(){
        val cell = ComputedCell(TestCellsRendering::formatted){
            Colour.Blue.buildCondition {
                it.text ==  "Blue_text"
            }
            Colour.Green.buildCondition {
                this.parameter == "Host_text"
            }
            Colour.Green.buildCondition {
                it.text ==  "Green_text"
            }
        }
        formatted.text = "Blue_text"
        var render = cell.render(this)
        assertTrue { render.contains(Colour.Blue) }

        formatted.text = "Not blue"
        parameter = "Host_text"
        render = cell.render(this)
        assertTrue { render.contains("Not blue") }
        assertTrue { render.contains(Colour.Green) }
    }

    @Test
    fun `Computed cells rendered correctly inside the row`(){

        val cell = ComputedCell(TestCellsRendering::formatted){ it.formattedString }
        val cell2 = ComputedCell(TestCellsRendering::plain){ it.text }
        val row = PrettyRow(cell, cell2)
        var render = row.render(this)
        var split = render.lines()
        assertEquals(1, split.size)
        assertTrue { render.contains(plain.text) }
        assertTrue { render.contains(formatted.formattedString) }
        assertTrue { render.contains(formatted.useColour) }

        val opt = RowOptions(Orientation.Vertical)
        row.applyOptions(opt)
        render = row.render(this)
        split = render.lines()
        assertEquals(2, split.size)
        assertTrue { split[0].contains(formatted.formattedString) }
        assertTrue { split[1].contains(plain.text) }
    }
}