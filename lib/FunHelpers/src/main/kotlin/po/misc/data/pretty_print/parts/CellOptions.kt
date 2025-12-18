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

    val renderKey:Boolean
    var plainText: Boolean
    var plainKey: Boolean
    val useForKey: String?

    val useSourceFormatting: Boolean

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
data class Style(
    var textStyle: TextStyle = TextStyle.Regular,
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
    override var style: Style = Style(),
    override var keyStyle : Style = Style(),
    override var renderKey:Boolean = true,
    private  val emptySpaceFiller: Char? = null,
    override var id: Enum<*>? = null
): CellOptions {

    constructor(alignment: Align):this(width =0, alignment)
    constructor(id: Enum<*>, alignment: Align = Align.LEFT):this(width =0, alignment, id = id)
    constructor(alignment: Align, colour: Colour):this(width = 0, alignment, Style(colour = colour))

    constructor(
        preset: CellPresets
    ):this(
        width =  0,
        alignment =  preset.align,
        style =  preset.style,
        keyStyle =  preset.keyStyle,
        renderKey = preset.renderKey,
    )

    constructor(
        rowOptions: RowOptions
    ): this(){
        val cellOptions = rowOptions.cellOptions
        if(cellOptions != null){
            width = cellOptions.width
            alignment = cellOptions.alignment
            style = cellOptions.style
            keyStyle = cellOptions.keyStyle
            id = cellOptions.id
        }
        plainText = rowOptions.plainText
    }

    override var plainText: Boolean = true


    override var plainKey: Boolean = false
    override var useForKey: String? = null
    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()

    override var useSourceFormatting: Boolean = false
    override var renderLeftBorder: Boolean = true
    override var renderRightBorder: Boolean = true

    override fun asOptions(width: Int): Options = this

    fun style(textStyle: TextStyle, colour: Colour, backgroundColour: BGColour? = null){
        style = Style(textStyle, colour, backgroundColour)
    }

    fun keyStyle(textStyle: TextStyle, colour: Colour, backgroundColour: BGColour? = null){
        keyStyle = Style(textStyle, colour, backgroundColour)
    }

    fun build(builder: Options.()-> Unit):Options{
        builder.invoke(this)
        return this
    }

    fun applyChanges(other: CellOptions?):Options{
        if(other != null){
            plainText = other.plainText
            plainKey = other.plainKey
            renderKey = other.renderKey
            useSourceFormatting = other.useSourceFormatting
            useForKey = other.useForKey
        }
        return this
    }

    fun usePlainValue(usePlain:Boolean): Options{
        plainText = usePlain
        return this
    }

    companion object
}

/**
 * Cell options for key–value pairs, adding separate styling for the key
 * segment of the cell and optional key visibility/customization.
 *
 * Keyed options are typically used for layouts such as:
 *     Name: John Doe
 *
 * @property keyStyleOptions Separate styling applied to the key portion.
 * @property renderKey Whether the key should be rendered.
 * @property useForKey Optional override for the displayed key label.
 */
//data class KeyedOptions(
//    override val width: Int = 0,
//    override var alignment: Align = Align.LEFT,
//    override var styleOptions: TextStyleOptions = TextStyleOptions(),
//    override var keyStyleOptions : TextStyleOptions = TextStyleOptions(),
//    override var renderKey: Boolean = true,
//    override var useForKey: String? = null,
//    private val emptySpaceFiller: Char? = null,
//    override val id: Enum<*>? = null
//): CellOptions {
//
//    constructor(
//        preset: KeyedPresets,
//        id: Enum<*>? = null
//    ):this(
//        width = 0,
//        preset.align,
//        preset.styleOptions,
//        preset.keyStyleOptions,
//        preset.renderKey,
//        id = id
//    )
//    constructor(cellOptions: CellOptions):this(cellOptions.width, cellOptions.alignment, cellOptions.styleOptions){
//        plainText = cellOptions.plainText
//    }
//    override val spaceFiller: Char get() = emptySpaceFiller.orDefault()
//    override var plainText: Boolean = false
//
//    override var plainKey: Boolean = false
//
//    override var renderLeftBorder: Boolean = true
//    override var renderRightBorder: Boolean = true
//
//
//    override var useSourceFormatting: Boolean = false
//
//    fun usePlainValue(usePlain:Boolean): KeyedOptions{
//        plainText = usePlain
//        return this
//    }
//
//    fun applyChanges(other: KeyedOptions){
//        plainText = other.plainText
//        plainKey = other.plainKey
//        renderKey = other.renderKey
//        useSourceFormatting = other.useSourceFormatting
//        useForKey = other.useForKey
//    }
//    override fun asOptions(): Options = Options(this)
//
//    companion object
//}