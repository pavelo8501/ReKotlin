package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.formatters.CompositeFormatter
import po.misc.data.pretty_print.formatters.DynamicTextFormatter
import po.misc.data.pretty_print.formatters.DynamicTextStyler
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.StaticModifiers
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.TextStyler


sealed class PrettyCellBase<P: StylePresets>(
    override var options : CommonCellOptions
) : BaseRenderer<P> {

    var index: Int = 0
        internal set

    val staticModifiers : StaticModifiers = StaticModifiers()
    var postfix: String? = null

    val borders : PrettyBorders = PrettyBorders('|', '|')

    val textFormatter: DynamicTextFormatter<P> = DynamicTextFormatter{ text, cell, ->
        if(postfix != null){
            "$text$postfix"
        }else{
            text
        }
    }
    val dynamicTextStyler: DynamicTextStyler<P> = DynamicTextStyler{ text, cell, ->
        val useStyle = options.styleOptions.style
        val useColour = options.styleOptions.colour
        val useBackgroundColour = options.styleOptions.backgroundColour
        TextStyler.style(text, applyColourIfExists = false, useStyle, useColour, useBackgroundColour)
    }
    val compositeFormatter: CompositeFormatter<P> = CompositeFormatter(textFormatter, dynamicTextStyler)

    private fun calculateEffectiveWidth(renderOptions: CommonRenderOptions): Int{
        val cellWidth = options.width
        val cellsCount = renderOptions.cellsCount
        val rowMaxWidth = renderOptions.rowMaxSize
        val align = options.alignment

        val cellAverageWidth: Int = rowMaxWidth / cellsCount

        if(cellWidth != 0 ){
            return cellWidth
        }
        return  when {
            align == Align.CENTER ->{
                cellAverageWidth
            }
            align != Align.CENTER  ->{
                0
            }
            else ->  {
                0
            }
        }
    }
    protected fun justifyText(text: String, renderOptions: CommonRenderOptions): String {
        val useWidth =  calculateEffectiveWidth(renderOptions)
        val useAlignment = options.alignment
        val useFiller = options.spaceFiller

        val aligned =  if(renderOptions.orientation == Orientation.Vertical){
            text
        }else{
            when (useAlignment) {
                Align.LEFT -> {
                    text.padEnd(useWidth, useFiller)
                }
                Align.RIGHT -> {
                    text.padStart(useWidth, useFiller)
                }
                Align.CENTER -> {
                    val fillerString = useFiller.toString()
                    val diff = useWidth - text.length
                    val left =  (diff / 2).coerceAtLeast(0)
                    val right = (diff - left).coerceAtLeast(0)
                    fillerString.repeat(left) + text + fillerString.repeat(right)
                }
            }
        }

        val bordered = borders.render(aligned, renderOptions)
        return bordered
    }

    open fun applyPreset(preset: P):PrettyCellBase<P>{
        options =  preset.toOptions()
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

    override fun render(content: String, renderOptions: CommonRenderOptions): String {
        val modified =  staticModifiers.modify(content)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }
    override fun render(formatted: FormattedPair, renderOptions: CommonRenderOptions): String {
        val usedText = if(renderOptions.usePlain){ formatted.text } else { formatted.formatedText }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  renderOptions)
        return final
    }

    fun render(content: String): String = render(content, CellRender(Orientation.Horizontal))

    companion object {

        fun build(width: Int, builder: PrettyCell.()-> Unit ): PrettyCell{
            val cell = PrettyCell(width)
            cell.builder()
            return cell
        }
        fun copyKeyParams(source: PrettyCellBase<*>, target: PrettyCellBase<*>):PrettyCellBase<*>{
            target.postfix = source.postfix
            target.options = source.options
            target.staticModifiers.addModifiers(source.staticModifiers.modifiers)
            when(source){
                is KeyedCell if target is KeyedCell -> {
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
