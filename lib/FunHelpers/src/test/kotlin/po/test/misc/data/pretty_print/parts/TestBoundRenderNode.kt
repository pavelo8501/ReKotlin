package po.test.misc.data.pretty_print.parts

import po.misc.data.output.output
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.options.ExtendedString
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.rendering.BoundRenderNode
import po.misc.reflection.displayName
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.reflect.KProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull


class TestBoundRenderNode: PrettyTest<TestBoundRenderNode>(true) {

    override val type: TypeToken<TestBoundRenderNode> = tokenOf()
    private val text: String = "Longer text should be trimmed to initial width of the cell"
    private val options = Options()
    private val rowOptions = RowOptions(Orientation.Horizontal)

    private fun keySegmentSize(property: KProperty<*>, separator: ExtendedString): Int{
        return property.displayName.length + separator.displaySize
    }

    @Test
    fun `Key size parameters`(){
        val node = BoundRenderNode(::text.toCell(), 0, 1, rowOptions)
        val cell = assertIs<PrettyCellBase<*>>(node.cell)
        val keySegSize = keySegmentSize(::text, cell.currentRenderOpts.keySeparator)
        val totalSize = keySegSize + text.length
        val render = node.render(this)
        val renderRec = assertNotNull(node.renderRecord)
        render.output(enableOutput)

        assertNotNull(cell.parameters)
        assertEquals(keySegSize, renderRec.plainKeySize)
        assertEquals(totalSize, node.width)
    }



}