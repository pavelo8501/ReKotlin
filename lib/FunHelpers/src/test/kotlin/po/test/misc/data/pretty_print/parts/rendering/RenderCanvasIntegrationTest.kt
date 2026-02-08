package po.test.misc.data.pretty_print.parts.rendering

import po.misc.callbacks.signal.listen
import po.misc.data.output.output
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.parts.common.Row
import po.misc.data.pretty_print.parts.options.BorderPresets
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.pretty_print.parts.render.LayerType
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderParameters
import po.misc.data.pretty_print.parts.render.RenderSnapshot
import po.misc.data.pretty_print.parts.rows.RowRenderPlanner
import po.misc.data.text_span.TextSpan
import po.misc.types.token.TypeToken
import po.misc.types.token.toToken
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class RenderCanvasIntegrationTest: PrettyTest<RenderCanvasIntegrationTest>(), RenderParameters{

    private class NodeMock(val cell: KeyedCell<RenderCanvasIntegrationTest>): CellParameters{
        override var contentWidth = 0
        init {
            cell.onContentRendered {
                contentWidth = it.plainLength
            }
        }
        fun render(receiver: RenderCanvasIntegrationTest): TextSpan{
           return with(cell){ renderInScope(receiver) }
        }
    }
    override val receiverType: TypeToken<RenderCanvasIntegrationTest> = toToken()

    private val self = this
    private val text1 = "Some text 1"
    private val text2 = "Some text 2"

    @Test
    fun `Merging render canvas produced by cells`() {
        val cell1 = ::text1.toCell()
        val cell2 = ::text2.toCell()
        cell1.borders(BorderPresets.Box)
        cell2.borders(BorderPresets.Box)
        val node1 = NodeMock(cell1)
        val node2 = NodeMock(cell2)
        val record1 = assertIs<RenderCanvas>(node1.render(self))
        val record2 = assertIs<RenderCanvas>(node2.render(self))

        val expectedLength = record1.plainLength + record2.plainLength
    }

    @Test
    fun `Merging render canvas produced by nodes inside a row`(){
        val cell1 = ::text1.toCell()
        cell1.borders(BorderPresets.Box)
        val row = buildRow(Row.Row1){
            add(cell1)
            options.borders(BorderPresets.Box)
        }
        val planner : RowRenderPlanner<RenderCanvasIntegrationTest> = row.planner
        planner.hooks.onRendered.onSignal {
            val snapshot: RenderSnapshot = it.createSnapshot()
            snapshot.output()
        }
        val renderData = with(row){ renderInScope(self) }
        renderData.layers[0].metaText
        renderData.output()

    }

}