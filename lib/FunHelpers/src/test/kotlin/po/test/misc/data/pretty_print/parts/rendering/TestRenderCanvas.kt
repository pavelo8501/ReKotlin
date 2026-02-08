package po.test.misc.data.pretty_print.parts.rendering

import po.misc.collections.asList
import po.misc.collections.second
import po.misc.data.output.output
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.render.*
import po.misc.data.strings.contains
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.colorize
import po.misc.data.text_span.OrderedText
import po.misc.testing.assertBlock
import po.misc.types.token.TypeToken
import po.misc.types.token.toToken
import po.test.misc.data.pretty_print.setup.PrettyTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestRenderCanvas: PrettyTest<TestRenderCanvas>() {

    override val receiverType: TypeToken<TestRenderCanvas> = toToken()


    private fun createDecorationLayer(): CanvasLayer{
        val topDecoration = "--------------".toPair(RenderRole.TopBorder)
        val content = "Content".toPair(RenderRole.Content)
        val bottomDecoration = "--------------".toPair(RenderRole.BottomBorder)
        return CanvasLayer(LayerType.Decoration, listOf(topDecoration,content, bottomDecoration))
    }

    @Test
    fun `Decoration layer`(){
        val content = "Some text".colorize(Colour.Yellow).toPair()
        val upperDecoration = "--------------".toPair()
        val lowerDecoration = upperDecoration.copy()
        val text = OrderedText(upperDecoration, content, lowerDecoration)
        val data = RenderCanvas(LayerType.Render, content.asList())
        data.createDecorationLayer(text)
        val layer0 =  data[0]
        val layer1 = data[1]
        assertEquals(LayerType.Render, layer0.layerType)
        assertEquals(LayerType.Decoration, layer1.layerType)
        assertEquals(content.plainLength, layer0.lineMaxLen, "Render layer length does not match text size")
        assertEquals(upperDecoration.plainLength, layer1.lineMaxLen, "Decoration layer length does not match upper decoration")
    }

    @Test
    fun `Multiple layers layers`(){
        val content = "Render 1 text".colorize(Colour.Yellow).toPair()
        val content2 = "Render 2 text".colorize(Colour.Yellow).toPair()
        val upperDecoration = "------------------".toPair()
        val lowerDecoration = upperDecoration.copy()
        val decoration1 = OrderedText(upperDecoration, content, lowerDecoration)
        val decoration2 = OrderedText(upperDecoration, content2, lowerDecoration)
        val data = RenderCanvas(LayerType.Render, content.asList())
        data.createDecorationLayer(decoration1)
        data.createRenderLayer(content2)
        data.createDecorationLayer(decoration2)

        assertEquals(4, data.layersCount)
        assertEquals(content.plain, data[0].plain)
        assertEquals(content2.plain, data[2].plain)
    }

    @Test
    fun `Merging canvas all added render canvas are normalized to LayerType Render pattern`() {
        val canva1 = RenderCanvas(LayerType.Render)
        val content = "Content".toPair(RenderRole.Content)
        canva1.addSpan(content)

        val line1 = "Line 1".toPair(RenderRole.TopBorder)
        val line2 = "Line 2".toPair(RenderRole.Content)
        val canva2 = RenderCanvas(LayerType.Render, listOf(line1, line2))
        canva1.mergeToActiveLayer(canva2)

        assertEquals(1, canva1.layers.size, "Layers count changed")
        assertNotNull(canva1.layers.firstOrNull()){layer->
            assertEquals(2, layer.lines.size, "Lines not added")
            assertEquals("Content", layer.lines[0].plain)
            assertTrue { layer.lines[1].contains("Line 1") && layer.lines[1].contains("Line 2") }
        }
    }

    @Test
    fun `Add layer work as expected`(){
        val content = "Content".toPair(RenderRole.Content)
        val canva = RenderCanvas(LayerType.Render,listOf(content))
        val decoration = createDecorationLayer()
        canva.addLayer(decoration)

        assertEquals(2, canva.layers.size, "Layers count mismatch")
        assertEquals(false, canva.layers[0].enabled)
        assertEquals(true, canva.layers[1].enabled)
    }
}