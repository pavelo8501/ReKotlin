package po.misc.data.pretty_print.parts.render

import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.strings.appendStyled
import po.misc.data.styles.StyleCode


interface BaseRenderParameters{
    val maxWidth: Int get() = 0
    val leftOffset: Int get() = 0
    val orientation: Orientation get() = Orientation.Horizontal
}

/**
 * @property maxWidth declared width of the container after decoration. Or empty space if no decoration applicable
 * @property leftOffset left margin between this and upper container
 */
interface RenderParameters: BaseRenderParameters{
    val contentWidth: Int get() = 0
    val index: Int get() = 0
    val layout : Layout get() = Layout.Compact
    val align: Align get() = Align.Left
}

interface CellParameters: BaseRenderParameters{
    val contentWidth: Int get() = 0
    val index: Int get() = 0
    val layout : Layout get() = Layout.Compact
    val availableWidth: Int get() = 0
}

interface StyleParameters{
    val keyStyle: StyleCode
    val style: StyleCode
}


