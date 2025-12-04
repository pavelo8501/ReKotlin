package po.misc.data.pretty_print.parts

import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.parts.CellOptions.TextStyleOptions
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle


interface CommonCellOptions{
    val width: Int
    val spaceFiller: Char
    val alignment: Align
    val styleOptions: TextStyleOptions
    val id: Enum<*>?
}

class CellOptions(
    override val width: Int = 0,
    override val alignment: Align = Align.LEFT,
    override val styleOptions: TextStyleOptions = TextStyleOptions(),
    private val emptySpaceFiller: Char? = null,
    override val id: Enum<*>? = null
): CommonCellOptions {

    class TextStyleOptions(
        val style: TextStyle = TextStyle.Regular,
        val colour: Colour? = null,
        val backgroundColour: BGColour? = null,
    )
    constructor(alignment: Align):this(width =0, alignment)
    constructor(id: Enum<*>, alignment: Align = Align.LEFT):this(width =0, alignment, id = id)
    constructor(alignment: Align, colour: Colour):this(width = 0, alignment, TextStyleOptions(colour = colour))

    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()

}