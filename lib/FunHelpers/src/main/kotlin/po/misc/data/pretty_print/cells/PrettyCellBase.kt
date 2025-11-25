package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.parts.Console80
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.formatters.CompositeFormatter
import po.misc.data.pretty_print.formatters.DynamicTextFormatter
import po.misc.data.pretty_print.formatters.DynamicTextStyler
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.StaticModifiers
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.strings.FormattedPair
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

    var defaults: RenderDefaults = Console80

    val borders : PrettyBorders = PrettyBorders('|', '|')
    override var options : CommonCellOptions = CellOptions()
        internal set

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
        val useColour = cell.options.styleOptions.colour
        val backGround: BGColour? = cell.backgroundColour
        TextStyler.style(text, applyColourIfExists = false, style =  textStyle, colour =  useColour, backGround)
    }
    val compositeFormatter: CompositeFormatter<P> = CompositeFormatter(textFormatter, dynamicTextStyler)

    private fun calculateEffectiveWidth(cellWidth: Int, rowMaxWidth: Int, cellsCount: Int): Int{
        if(cellWidth == 0){
            if(cellsCount <= 1){
                return rowMaxWidth
            }else{
                return rowMaxWidth / cellsCount
            }
        }
        return cellWidth
    }
    protected fun justifyText(text: String, renderOptions: RenderOptions): String {
        val useWidth =  calculateEffectiveWidth(options.width, renderOptions.rowMaxSize,  renderOptions.cellsCount)
        val useAlignment = options.alignment
        val useFiller = options.spaceFiller

        val aligned = when (useAlignment) {
            Align.LEFT -> {
                text.padEnd(useWidth, useFiller)
            }
            Align.RIGHT -> {
                text.padStart(useWidth, useFiller)
            }
            Align.CENTER -> {
                val filletString = useFiller.toString()
                val diff = useWidth - text.length
                val left = diff / 2
                val right = diff - left
                filletString.repeat(left) + text + filletString.repeat(right)
            }
        }
        return borders.render(aligned, renderOptions)
    }

    open fun applyPreset(preset: P):PrettyCellBase<P>{
        options =  preset.toOptions(width)
        return this
    }

    override fun addModifiers(vararg modifiers: TextModifier){
        staticModifiers.addModifiers(modifiers.toList())
    }

    fun colourConditions(builder: DynamicColourModifier.()-> Unit){
        val dynamicColourModifier = DynamicColourModifier()
        dynamicColourModifier.builder()
        staticModifiers.addModifier(dynamicColourModifier)
    }
    fun colourConditions(vararg  conditions : DynamicColourModifier.DynamicColourCondition){
        val dynamicColourModifier = DynamicColourModifier(*conditions)
        staticModifiers.addModifier(dynamicColourModifier)
    }

    override fun render(content: String): String {
        val modified =  staticModifiers.modify(content)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  RenderOptions())
        return final
    }
    override fun render(formatted: FormattedPair, renderOptions: RenderOptions): String {
        val usedText = if(renderOptions.usePlain){ formatted.text } else { formatted.formatedText }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
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
            target.options = source.options
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
                }
                is ComputedCell<*> if target is ComputedCell<*> -> {
                    target
                }
                else -> { }
            }
            return target
        }

        fun <T:PrettyCellBase<P>, P: StylePresets> copyParameters(source:T, target :T):T{
            target.staticModifiers.clear()
            target.staticModifiers.addModifiers(source.staticModifiers.modifiers)
            target.options = source.options
            return target
        }
    }
}
