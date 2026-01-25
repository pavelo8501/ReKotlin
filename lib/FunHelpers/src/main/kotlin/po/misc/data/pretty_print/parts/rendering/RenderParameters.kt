package po.misc.data.pretty_print.parts.rendering

import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.styles.StyleCode


interface BaseRenderParameters{
    val contentWidth: Int
    val maxWidth: Int
    val leftOffset: Int
}

/**
 * @property contentWidth Effective width after render
 * @property maxWidth declared width of the container after decoration. Or empty space if no decoration applicable
 * @property leftOffset left margin between this and upper container
 */
interface RenderParameters: BaseRenderParameters{
    val index: Int
    val layout : Layout
    val orientation: Orientation
    val align: Align
}

interface CellParameters: BaseRenderParameters{
    val index: Int
    val layout : Layout
    val availableWidth: Int
  //  val keySegmentSize: Int
}

class RenderSnapshot(
     val rendererName:String,
     parameters: KeyRenderParameters
): BaseRenderParameters {
    override val contentWidth: Int = parameters.contentWidth
    override val maxWidth: Int = parameters.maxWidth
    override val leftOffset: Int = parameters.leftOffset
    val orientation: Orientation = parameters.orientation
    val index: Int = parameters.index
}

interface StyleParameters{
    val keyStyle: StyleCode
    val style: StyleCode
}


