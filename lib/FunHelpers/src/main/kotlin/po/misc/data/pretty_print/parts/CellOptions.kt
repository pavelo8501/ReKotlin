package po.misc.data.pretty_print.parts

import po.misc.data.helpers.orDefault
import po.misc.data.pretty_print.parts.CellOptions.TextStyleOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle


sealed interface CommonCellOptions: PrettyOptions{
    val width: Int
    val spaceFiller: Char
    val alignment: Align
    val styleOptions: TextStyleOptions
    val id: Enum<*>?

    val usePlain: Boolean
    val orientation: Orientation
    val renderLeftBorder: Boolean
    val renderRightBorder: Boolean
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

    constructor(commonRowOptions: CommonRowOptions):this(){
        orientation = commonRowOptions.orientation
    }

    override var usePlain: Boolean = false
    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()
    override var orientation: Orientation = Orientation.Horizontal
    var cellsCount: Int = 1

    override var renderLeftBorder: Boolean = true
    override var renderRightBorder: Boolean = true

}

data class KeyedCellOptions(
    override val width: Int = 0,
    override val alignment: Align = Align.LEFT,
    override val styleOptions: TextStyleOptions = TextStyleOptions(),
    val keyStyleOptions : TextStyleOptions = TextStyleOptions(),
    val showKey: Boolean = true,
    val useKeyName: String? = null,
    private val emptySpaceFiller: Char? = null,
    override val id: Enum<*>? = null
): CommonCellOptions {

    constructor(preset: KeyedPresets, id: Enum<*>? = null):this(width = 0, preset.align, preset.styleOption(), preset.keyStyleOption(), id = id)
    constructor(cellOptions: CellOptions):this(cellOptions.width, cellOptions.alignment, cellOptions.styleOptions)
    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()

    override var orientation: Orientation = Orientation.Horizontal
    override var usePlain: Boolean = false
    override var renderLeftBorder: Boolean = true
    override var renderRightBorder: Boolean = true

    fun  toCellOptions(): CellOptions{
      return   CellOptions(width, alignment, styleOptions)
    }
}