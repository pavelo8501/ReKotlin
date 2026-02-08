package po.test.misc.data.pretty_print.parts.rendering

import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.ViewPortSize
import po.misc.data.pretty_print.parts.render.KeyParameters
import po.misc.data.pretty_print.parts.rows.Layout
import kotlin.test.Test
import kotlin.test.assertEquals

class TestKeyRenderParameters {

    private val stretchedOptions = RowOptions(Orientation.Horizontal, Layout.Stretch, ViewPortSize.Console40)
    private val compactOptions = RowOptions(Orientation.Horizontal, Layout.Compact, ViewPortSize.Console40)

    @Test
    fun `ViewportsSize sets lowest bound`(){
        val params = KeyParameters()
        params.initByOptions(compactOptions)

        params.updateWidth(30)
        assertEquals(Layout.Compact, params.layout)
        assertEquals(30, params.contentWidth)

        params.updateWidth(50)
        assertEquals(50, params.contentWidth)
        assertEquals(ViewPortSize.Console40.size, params.maxWidth)
    }

    @Test
    fun `ContentSize does not influence max width if layout Stretched`(){
        val params = KeyParameters(stretchedOptions)
        params.updateWidth(30)
        assertEquals(30, params.contentWidth)
        assertEquals(ViewPortSize.Console40.size, params.maxWidth, "Max width not initialized")

        params.updateWidth(50)
        assertEquals(50, params.contentWidth)
        assertEquals(ViewPortSize.Console40.size, params.maxWidth, "Max width changed")
    }

    @Test
    fun `Constraint have priority`(){

        val upperParams = KeyParameters(compactOptions)
        val params = KeyParameters(RowOptions(Orientation.Horizontal, Layout.Stretch, ViewPortSize.Console80))
        params.implyConstraints(upperParams)
        params.updateWidth(70)

        assertEquals(upperParams.maxWidth, params.containerMaxWidth, "Container width constraint set")
        assertEquals(70, params.contentWidth, "ContentWidth not updated")
        assertEquals(upperParams.maxWidth, params.maxWidth, "ContentWidth not updated")
    }

}