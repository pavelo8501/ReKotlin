package po.misc.data.pretty_print.parts.render

import po.misc.collections.filterIsSubInstance
import po.misc.data.Separator
import po.misc.data.StringModifyParams
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.strings.appendStyled
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.OrderedText
import po.misc.data.text_span.SpanRole
import po.misc.data.text_span.TextSpan

class CanvasLayer(
    val layerType: LayerType,
    initialSpans:List<TextSpan> = emptyList()
): OrderedText() {

    var displayOrientation: Orientation = Orientation.Vertical
        internal set

    val layerRoles: List<RenderRole> get() {
       return lines.mapNotNull { it.role }.filterIsSubInstance<RenderRole>().distinct()
    }
    var enabled: Boolean = true
         internal set

    override val text: TextSpan get() {
        return text(displayOrientation)
    }

    val metaText: String get() {
       return buildString {
          val params = StringModifyParams(Separator(", "))
          appendStyled(params, ::layerType)
          appendStyled(params, "Roles count ${layerRoles.size}", "Lines count ${lines.size}")
        }
    }

    init {
        appendAll(initialSpans)
        if(layerType == LayerType.Render){
            displayOrientation = Orientation.Horizontal
        }
    }
    fun normalizeToRoles(roles: List<RenderRole>){
        roles.forEach {role->
            val lineByRole = lines.firstOrNull { it.role == role }
            if(lineByRole == null){
                when(role) {
                    is RenderRole.TopBorder -> {
                        linesBacking.add(0, MutablePair(role = role))
                    }
                    is RenderRole.BottomBorder -> {
                        linesBacking.add(MutablePair(role = role))
                    }
                    is RenderRole.Content -> {}
                }
            }
        }
    }
    fun merge(layer: CanvasLayer){
        when(layerType) {
            LayerType.Dynamic -> {
                linesBacking.addAll(layer.linesBacking)
            }
            LayerType.Render -> {
                val normalized = layer.toMutable(Orientation.Horizontal, RenderRole.Content)
                linesBacking.add(normalized)
            }
            LayerType.Decoration-> {
                linesBacking.clear()
                linesBacking.addAll(layer.linesBacking)
            }
        }
    }

    override fun merge(other: OrderedText, separator: Separator) {
        if (other is CanvasLayer){
            merge(layer = other)
        }else{
            linesBacking.clear()
            linesBacking.addAll(other.linesBacking)
        }
    }
    override fun append(orderedText: OrderedText){
        merge(orderedText)
    }
    operator fun get(renderRole: RenderRole): TextSpan{
        return lines.firstOrNull {
            it.role == renderRole
        }?:run {
            throw IllegalArgumentException("Lines does not contain any of role $renderRole")
        }
    }
    override fun toString(): String = metaText
    override fun copy(newRole: SpanRole?): CanvasLayer {
        val layer = CanvasLayer(layerType, lines)
        if(newRole != null) {
            layer.linesBacking.filter { it.role == newRole }
        }
       return layer
    }
}