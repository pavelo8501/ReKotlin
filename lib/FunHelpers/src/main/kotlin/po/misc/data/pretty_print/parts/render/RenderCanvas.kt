package po.misc.data.pretty_print.parts.render

import po.misc.data.Normalizable
import po.misc.data.Postfix
import po.misc.data.Separator
import po.misc.data.Styled
import po.misc.data.styles.SpecialChars
import po.misc.data.text_span.OrderedText
import po.misc.data.text_span.TextKind
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.joinSpans

class RenderCanvas(
    initialLayerType: LayerType,
    initialSpans: List<TextSpan> = emptyList()
): Styled, Normalizable{

    internal val layersBacking: MutableList<CanvasLayer> = mutableListOf()

    val layers: List<CanvasLayer> get() = layersBacking
    val activeLayer: CanvasLayer get() =  layers.last()
    val activeLayers: List<CanvasLayer> get() =  layers.filter { it.enabled }

    val renderLayersCount: Int get() = layers.count { it.layerType == LayerType.Render }
    val decorationLayersCount: Int get() = layers.count { it.layerType == LayerType.Decoration }
    val layersCount: Int get() = layers.size
    val lineMaxLen: Int get() = activeLayers.maxOfOrNull { it.lineMaxLen }?:0

    override val textSpan: TextSpan get() {
       return activeLayers.joinSpans(Postfix(SpecialChars.WHITESPACE))
    }
    val plainLength: Int get() = textSpan.plainLength
    val metaText: String get() {
        return buildString {
            append("RenderLayers:${renderLayersCount} ")
            append("DecorationLayers:${decorationLayersCount}")
        }
    }

    init {
        createLayer(initialLayerType, initialSpans)
    }

    fun addSpan(span: TextSpan){
        if(span is CanvasLayer){
            addLayer(span, disableExistent = true)
        }else{
            activeLayer.append(span)
        }
    }

    fun createLayer(layerType: LayerType, spans: List<TextSpan>): CanvasLayer {
        val layer = CanvasLayer(layerType, spans)
        layersBacking.add(layer)
        return layer
    }
    fun createLayer(layerType: LayerType, text: OrderedText): CanvasLayer {
        val layer = CanvasLayer(layerType, text.lines)
        layersBacking.add(layer)
        return layer
    }

    fun addLayer(layer: CanvasLayer, disableExistent: Boolean = false) {
        layers.forEach { it.enabled = !disableExistent }
        val dynamicLayer = layers.lastOrNull { it.layerType == LayerType.Dynamic }
        if(dynamicLayer != null && dynamicLayer.lines.isEmpty()){
            layersBacking.clear()
            layersBacking.add(layer)
        }else{
            layersBacking.add(layer)
        }
    }
    fun addLayers(layer: List<CanvasLayer>, disableExistent: Boolean = true) {
        layer.forEach { addLayer(it, disableExistent) }
    }

    fun createDecorationLayer(text: OrderedText): CanvasLayer = createLayer(LayerType.Decoration, text)
    fun createDecorationLayer(spans: List<TextSpan>): CanvasLayer = createLayer(LayerType.Decoration, spans)
    fun createRenderLayer(vararg spans: TextSpan): CanvasLayer = createLayer(LayerType.Render, spans.toList())

    /**
     * Merges RenderCanvas active layer to current active layer
     */
    fun mergeToActiveLayer(canvas: RenderCanvas){
        activeLayer.merge(canvas.activeLayer)
    }
    fun mergeAllToActiveLayer(canvas: List<RenderCanvas>){
        canvas.forEach { mergeToActiveLayer(it) }
    }
    fun output(layerType: LayerType, textKind: TextKind = TextKind.Styled){
        println(toString(layerType, textKind))
    }
    fun toString(layerType: LayerType, textKind: TextKind = TextKind.Styled): String{
        return if(textKind == TextKind.Styled){
            layers.lastOrNull{ it.layerType == layerType }?.styled?:layers.last().styled
        }else{
            layers.lastOrNull{ it.layerType == layerType }?.plain?:layers.last().plain
        }
    }


    override fun normalize(){
        if(layers.hasRoleBorder()){
            for(layer in layers){
                layer.normalizeToRoles(RenderRole.roles)
            }
        }
    }

    operator fun get(layerIndex: Int): CanvasLayer{
       return layers.getOrElse(layerIndex){layers.last()}
    }

    operator fun get(renderRole: RenderRole): CanvasLayer{
        return layers.firstOrNull {
            it.role == renderRole
        }?:run {
            throw IllegalArgumentException("Layers does not contain layer role $renderRole")
        }
    }

    fun clear(){
        layersBacking.clear()
    }

    override fun toString(): String = metaText

    companion object

}