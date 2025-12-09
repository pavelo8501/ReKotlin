package po.misc.data.pretty_print.parts

import po.misc.data.helpers.orDefault
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import javax.swing.text.html.Option

/**
 * Full configuration set for a single rendered cell.
 *
 * Implementations specify layout rules (e.g., width, alignment),
 * appearance (style and colours), and rendering flags (orientation,
 * borders, plain rendering). These options are consumed during grid
 * construction to determine how a cell should be displayed.
 */
sealed interface CellOptions: CommonCellOptions{
    val width: Int
    val spaceFiller: Char
    val alignment: Align
    val id: Enum<*>?

    var usePlain: Boolean
    val renderLeftBorder: Boolean
    val renderRightBorder: Boolean
}

/**
 * Visual styling options for cell content.
 *
 * @property style Text style (regular, bold, italic, etc.)
 * @property colour Foreground text colour, or `null` to use default terminal colour.
 * @property backgroundColour Background fill colour, or `null` if not applied.
 */
data class TextStyleOptions(
    var style: TextStyle = TextStyle.Regular,
    var colour: Colour? = null,
    var backgroundColour: BGColour? = null,
)

/**
 * Standard implementation of [CellOptions].
 *
 * Represents the most common configuration used by value cells.
 * Allows specifying alignment, width, style, colours, and layout flags.
 *
 * Several convenience constructors exist for ergonomics:
 * - using only alignment,
 * - using alignment + colour,
 * - binding an enum ID,
 * - constructing from row-level options such as [CommonRowOptions].
 */
class Options(
    override var width: Int = 0,
    override var alignment: Align = Align.LEFT,
    override var styleOptions: TextStyleOptions = TextStyleOptions(),
    private  val emptySpaceFiller: Char? = null,
    override var id: Enum<*>? = null
): CellOptions {

    constructor(alignment: Align):this(width =0, alignment)
    constructor(id: Enum<*>, alignment: Align = Align.LEFT):this(width =0, alignment, id = id)
    constructor(alignment: Align, colour: Colour):this(width = 0, alignment, TextStyleOptions(colour = colour))
    constructor(keyedOptions: KeyedOptions):this(
        keyedOptions.width,
        keyedOptions.alignment,
        keyedOptions.styleOptions,
        id = keyedOptions.id
    )
    constructor(rowOptions: RowOptions): this(){
        val cellOptions = rowOptions.cellOptions
        if(cellOptions != null){
            width = cellOptions.width
            alignment = cellOptions.alignment
            styleOptions = cellOptions.styleOptions
            id = cellOptions.id
        }
        usePlain = rowOptions.usePlain
    }

    override var usePlain: Boolean = true
    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()

    var cellsCount: Int = 1
    override var renderLeftBorder: Boolean = true
    override var renderRightBorder: Boolean = true

    override fun asOptions(): Options = this

    fun build(builder: Options.()-> Unit):Options{
        builder.invoke(this)
        return this
    }

}

/**
 * Cell options for key–value pairs, adding separate styling for the key
 * segment of the cell and optional key visibility/customization.
 *
 * Keyed options are typically used for layouts such as:
 *     Name: John Doe
 *
 * @property keyStyleOptions Separate styling applied to the key portion.
 * @property showKey Whether the key should be rendered.
 * @property useKeyName Optional override for the displayed key label.
 */
data class KeyedOptions(
    override val width: Int = 0,
    override val alignment: Align = Align.LEFT,
    override val styleOptions: TextStyleOptions = TextStyleOptions(),
    val keyStyleOptions : TextStyleOptions = TextStyleOptions(),
    val showKey: Boolean = true,
    val useKeyName: String? = null,
    private val emptySpaceFiller: Char? = null,
    override val id: Enum<*>? = null
): CellOptions {

    constructor(preset: KeyedPresets, id: Enum<*>? = null):this(width = 0, preset.align, preset.styleOptions, preset.keyStyleOptions, id = id)
    constructor(cellOptions: CellOptions):this(cellOptions.width, cellOptions.alignment, cellOptions.styleOptions){
        usePlain = cellOptions.usePlain

    }

    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()
    override var usePlain: Boolean = false
    override var renderLeftBorder: Boolean = true
    override var renderRightBorder: Boolean = true

    override fun asOptions(): Options = Options(this)
}