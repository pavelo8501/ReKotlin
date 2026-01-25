package po.misc.data.pretty_print.parts.options

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.parts.common.Separator
import po.misc.data.pretty_print.parts.rendering.StyleParameters
import po.misc.data.strings.appendGroup
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle


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
    var width: Int = 0,
    override var align: Align = Align.Left,
    override var style: Style = Style(),
    override var keyStyle : Style = Style(),
    private  val emptySpaceFiller: Char? = null
): CellOptions, PrettyPrint, StyleParameters {

    constructor(alignment: Align):this(width =0, alignment)
    constructor(alignment: Align, colour: Colour):this(width = 0, alignment, Style(colour = colour))

    constructor(
        preset: CellPresets
    ):this(
        width =  0,
        align =  preset.align,
        style =  preset.style,
        keyStyle =  preset.keyStyle,
    ){
        renderKey = preset.renderKey
    }

    constructor(
        rowOptions: RowOptions
    ): this(){
        val cellOptions = rowOptions.cellOptions
        if(cellOptions != null){
            width = cellOptions.width
            align = cellOptions.align
            style = cellOptions.style
            keyStyle = cellOptions.keyStyle
        }
    }

    var plainText: Boolean = false
        set(value) {
            field = value
            if(value){
                sourceFormat = false
            }
            notifYModified()
        }

    var sourceFormat: Boolean = false
        set(value) {
            field = value
            if(value){
                plainText = false
            }
            notifYModified()
        }

    override var plainKey: Boolean = false
    var keyText: String? = null

    var trimText:Separator = Separator("...")

    var keySeparator: Separator = Separator(':')

    override var renderKey:Boolean = false
        set(value) {
            field = value
            keySeparator.enabled = value
        }

    var spaceFiller: Separator = Separator(" ")


    var renderLeftBorder: Boolean = true
    var renderRightBorder: Boolean = true

    private var onUserModified: (() -> Unit)? = null

    private fun initByPreset(preset: CellPresets): Options{
        align =  preset.align
        style =  preset.style
        keyStyle =  preset.keyStyle
        renderKey = preset.renderKey
        return this
    }

    private fun notifYModified(){
        onUserModified?.let {
            it.invoke()
            onUserModified = null
        }
    }

    internal fun onUserModified(callback: () -> Unit): Options{
        onUserModified = callback
        return this
    }

    override fun asOptions(width: Int): Options = this

    fun style(textStyle: TextStyle, colour: Colour, backgroundColour: BGColour? = null){
        style = Style(textStyle, colour, backgroundColour)
    }
    fun keyStyle(textStyle: TextStyle, colour: Colour, backgroundColour: BGColour? = null){
        keyStyle = Style(textStyle, colour, backgroundColour)
    }

    fun applyChanges(other: CellOptions?):Options{
         if(other == null){
             return this
         }
         when(other){
             is Options -> {
                 plainText = other.plainText
                 plainKey = other.plainKey
                 renderKey = other.renderKey
                 sourceFormat = other.sourceFormat
                 keyText = other.keyText
             }
             is CellPresets-> {
                 initByPreset(other)
             }
         }
        return this
    }
    fun usePlainValue(usePlain:Boolean): Options{
        plainText = usePlain
        return this
    }

    fun copy(): Options{
       return Options(
            width = this.width,
            align = this.align,
            style = this.style.copy(),
            keyStyle = this.keyStyle.copy(),
            emptySpaceFiller = this.emptySpaceFiller,
        ).also {
            it.plainText = this.plainText
            it.plainKey = this.plainKey
            it.keyText = this.keyText
            it.keySeparator = this.keySeparator.copy()
            it.spaceFiller = this.spaceFiller
            it.sourceFormat  = this.sourceFormat
            it.renderKey = this.renderKey
            it.renderLeftBorder = this.renderLeftBorder
            it.renderRightBorder = this.renderRightBorder
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other!is Options) return false
        if(other.plainText != plainText ) return false
        if(other.plainKey != plainKey) return false
        if(other.renderKey != renderKey) return false
        if(other.keyText != keyText) return false
        if(other.sourceFormat != sourceFormat) return false
        if(other.renderLeftBorder != renderLeftBorder) return false
        if(other.renderRightBorder != renderRightBorder) return false
        if(other.width != width) return false
        if(other.style != style) return false
        if(other.keyStyle != keyStyle) return false
        return true
    }
    override fun hashCode(): Int {
        var result = width
        result = 31 * result + renderKey.hashCode()
        result = 31 * result + (emptySpaceFiller?.hashCode() ?: 0)
        result = 31 * result + plainText.hashCode()
        result = 31 * result + plainKey.hashCode()
        result = 31 * result + spaceFiller.hashCode()
        result = 31 * result + sourceFormat.hashCode()
        result = 31 * result + renderLeftBorder.hashCode()
        result = 31 * result + renderRightBorder.hashCode()
        result = 31 * result + align.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + keyStyle.hashCode()
        result = 31 * result + (keyText?.hashCode() ?: 0)
        return result
    }

    override val formattedString: String
        get() = buildString {
            appendGroup("Options[", "]", ::renderKey)
        }

    companion object
}
