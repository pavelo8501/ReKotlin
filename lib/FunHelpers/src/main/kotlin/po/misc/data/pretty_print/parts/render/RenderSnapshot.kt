package po.misc.data.pretty_print.parts.render

import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.strings.appendStyled

class RenderSnapshot(
    val rendererName:String,
    parameters: KeyParameters
): RenderParameters {

    override val contentWidth: Int = parameters.contentWidth
    override val maxWidth: Int = parameters.maxWidth
    override val leftOffset: Int = parameters.leftOffset
    override val orientation: Orientation = parameters.orientation
    override val index: Int = parameters.index

    val metaText: String get() {
        return buildString {
            appendStyled("RenderSnapshot[$rendererName]",  ::contentWidth, ::maxWidth, ::leftOffset, ::orientation, ::index)
        }
    }
    override fun toString(): String = metaText
}