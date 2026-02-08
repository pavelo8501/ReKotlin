package po.test.misc.data.pretty_print.parts.rendering


import po.misc.data.pretty_print.parts.render.CanvasLayer
import po.misc.data.pretty_print.parts.render.LayerType
import po.misc.data.pretty_print.parts.render.RenderRole
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.OrderedText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CanvasLayerTest : TextStyler {

    private fun createDecorationLayer(): CanvasLayer{
        val topDecoration = "--------------".toPair(RenderRole.TopBorder)
        val content = "Content".toPair(RenderRole.Content)
        val bottomDecoration = "--------------".toPair(RenderRole.TopBorder)
        return CanvasLayer(LayerType.Decoration, listOf(topDecoration,content, bottomDecoration))
    }

    @Test
    fun `Add text span`(){
        val layer = CanvasLayer(LayerType.Render)
        val content = "Content".toPair(RenderRole.Content)
        layer.append(content)

        assertEquals(1, layer.layerRoles.size)
        assertIs<RenderRole.Content>(layer.layerRoles[0])
    }

    @Test
    fun `Displayed text`() {
        val layer = CanvasLayer(LayerType.Render)
        val content = "Content".toPair(RenderRole.Content)
        layer.append(content)
        assertEquals(content.plain, layer.plain)


    }

    @Test
    fun `Add ordered text with normalization`(){
        val layer = CanvasLayer(LayerType.Decoration)
        val border1 = "-----".toPair(RenderRole.TopBorder)
        val line1 = "Line 1".toPair(RenderRole.Content)
        val text = OrderedText(border1, line1)
        layer.append(text)
        assertEquals(2, layer.layerRoles.size)
        assertIs<RenderRole.TopBorder>(layer.layerRoles[0])
        assertIs<RenderRole.Content>(layer.layerRoles[1])
    }

    @Test
    fun `Add ordered text with normalization to render type`(){
        val layer = CanvasLayer(LayerType.Render)
        val border1 = "-----".toPair(RenderRole.TopBorder)
        val line1 = "Line 1".toPair(RenderRole.Content)
        val text = OrderedText(border1, line1)
        layer.append(text)
        assertEquals(1, layer.layerRoles.size)
        assertIs<RenderRole.Content>(layer.layerRoles[0])
    }

    @Test
    fun `Add ordered text withe existent record with normalization`(){
        val layer = CanvasLayer(LayerType.Render)
        val content = "Content".toPair(RenderRole.Content)
        layer.append(content)

        val border1 = "-----".toPair(RenderRole.TopBorder)
        val line1 = "Line 1".toPair(RenderRole.Content)
        val text = OrderedText(border1, line1)

        layer.append(text)
        assertEquals(1, layer.layerRoles.size)
        assertIs<RenderRole.Content>(layer.layerRoles[0])
    }

    @Test
    fun `Dynamic type layer does not normalize`(){
        val layer = CanvasLayer(LayerType.Dynamic)
        val decoration = createDecorationLayer()
        layer.append(decoration)
        assertEquals(3, layer.linesCount)

    }

}