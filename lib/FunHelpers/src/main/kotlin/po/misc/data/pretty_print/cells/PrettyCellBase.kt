package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.Align
import po.misc.data.pretty_print.Console80
import po.misc.data.pretty_print.RenderDefaults
import po.misc.data.pretty_print.formatters.CompositeFormatter
import po.misc.data.pretty_print.formatters.DynamicTextFormatter
import po.misc.data.pretty_print.formatters.DynamicTextStyler
import po.misc.data.pretty_print.formatters.StaticModifiers
import po.misc.data.pretty_print.formatters.TextModifier
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.styles.BGColour
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyle
import po.misc.data.styles.TextStyler

sealed class PrettyCellBase<P: StylePresets>(
    val width: Int,
    var align: Align = Align.LEFT
) : BaseRenderer<P> {

    val staticModifiers : StaticModifiers = StaticModifiers()

    var colour: Colour? = null
    var backgroundColour: BGColour? = null

    var style: TextStyle = TextStyle.Regular
    var postfix: String? = null
    var emptySpaceFiller: Char? = null
    var defaults: RenderDefaults = Console80

    abstract val builder: (Int, Any) ->  PrettyCellBase<P>

    val textFormatter: DynamicTextFormatter<P> = DynamicTextFormatter{ text, cell, ->
        val presetPostfix = preset?.postfix
        val resultingPostfix = postfix?:presetPostfix
        if(resultingPostfix != null){
            "$text $resultingPostfix"
        }else{
            text
        }
    }
    val dynamicTextStyler: DynamicTextStyler<P> = DynamicTextStyler{ text, cell, ->
        val textStyle = cell.style
        val colour: Colour? = cell.colour
        val backGround: BGColour? = cell.backgroundColour
        TextStyler.style(text, applyColourIfExists = false, style =  textStyle, colour =  colour, backGround)
    }
    val compositeFormatter: CompositeFormatter<P> = CompositeFormatter(textFormatter, dynamicTextStyler)

    protected fun applyWidth(text: String, width: Int): String {
        val filler = emptySpaceFiller
        return when (align) {
            Align.LEFT -> {
                if(filler == null){
                    text.padEnd(width)
                }else{
                    val withWhitespace = "$text "
                    withWhitespace.padEnd(width, filler)
                }
            }
            Align.RIGHT -> {
                if(filler == null){
                    text.padStart(width)
                }else{
                    text.padStart(width, filler)
                }
            }
            Align.CENTER -> {
                val filletString = filler?.toString()?:""
                val diff = width - text.length
                val left = diff / 2
                val right = diff - left
                filletString.repeat(left) + text + filletString.repeat(right)
            }
        }
    }

    override fun applyTextModifiers(vararg modifiers: TextModifier){
        staticModifiers.addModifiers(modifiers.toList())
    }

    /**
     * Apply normalization → postfix → styling → width.
     */
    override fun render(content: String): String {

        val modified =  staticModifiers.modify(content)
        val formatted =  compositeFormatter.format(modified, this)
        val usedWidth =  width.coerceAtMost(defaults.DEFAULT_WIDTH)
        val final = applyWidth(formatted, usedWidth)
        return final
    }

    companion object {

        fun build(width: Int, builder: PrettyCell.()-> Unit ): PrettyCell{
            val cell = PrettyCell(width)
            cell.builder()
            return cell
        }

        fun copyKeyParams(source: PrettyCellBase<*>, target: PrettyCellBase<*>):PrettyCellBase<*>{
            target.postfix = source.postfix
            target.emptySpaceFiller = source.emptySpaceFiller
            target.colour = source.colour
            target.backgroundColour = source.backgroundColour
            target.style = source.style
            target.staticModifiers.addModifiers(source.staticModifiers.modifiers)
            when(source){
                is PrettyCell if target is PrettyCell ->{
                    target.preset = source.preset
                }

                is KeyedCell if target is KeyedCell -> {
                    target.preset = source.preset
                    target.property = source.property
                    target.options = source.options
                }
                else -> { }
            }
            return target
        }
    }
}
