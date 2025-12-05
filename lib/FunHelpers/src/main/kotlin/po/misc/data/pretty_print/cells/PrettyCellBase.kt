package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.formatters.CompositeFormatter
import po.misc.data.pretty_print.formatters.DynamicTextFormatter
import po.misc.data.pretty_print.formatters.DynamicTextStyler
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.StaticModifiers
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CellRender
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.presets.StylePresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.TextStyler


sealed class PrettyCellBase<P: StylePresets>(
    override var cellOptions : CommonCellOptions,
    val row: PrettyRow<*>?
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
        val useStyle = cellOptions.styleOptions.style
        val useColour = cellOptions.styleOptions.colour
        val useBackgroundColour = cellOptions.styleOptions.backgroundColour
        TextStyler.style(text, applyColourIfExists = false, useStyle, useColour, useBackgroundColour)
    }
    val compositeFormatter: CompositeFormatter<P> = CompositeFormatter(textFormatter, dynamicTextStyler)

    private fun calculateEffectiveWidth(renderOptions: CommonCellOptions): Int{
        val cellWidth = cellOptions.width
        val cellsCount =  row?.cells?.size?:1

        val rowMaxWidth = 0
        val align = cellOptions.alignment

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
    protected fun justifyText(text: String, renderOptions: CommonCellOptions): String {
        val useWidth =  calculateEffectiveWidth(renderOptions)
        val useAlignment = cellOptions.alignment
        val useFiller = cellOptions.spaceFiller

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
        cellOptions =  preset.toOptions()
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

    override fun render(content: String, commonOptions: CommonCellOptions?): String {
        val options = PrettyHelper.toCellOptionsOrDefault(commonOptions, PrettyHelper.toCellOptions(cellOptions))
        val modified =  staticModifiers.modify(content)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    override fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String {
        val usePlain = true
        val options = PrettyHelper.toCellOptionsOrDefault(commonOptions, PrettyHelper.toCellOptions(cellOptions))
        val usedText = if(usePlain){ formatted.text } else { formatted.formatedText }
        val modified =  staticModifiers.modify(usedText)
        val formatted =  compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }

    fun render(content: String): String = render(content, CellOptions(Orientation.Horizontal))

    companion object {

//        fun build(width: Int, builder: PrettyCell.()-> Unit ): PrettyCell{
//            val cell = PrettyCell(width)
//            cell.builder()
//            return cell
//        }

//        fun copyKeyParams(source: PrettyCellBase<*>, target: PrettyCellBase<*>):PrettyCellBase<*>{
//            target.postfix = source.postfix
//            target.options = source.options
//            target.staticModifiers.addModifiers(source.staticModifiers.modifiers)
//            when(source){
//                is KeyedCell if target is KeyedCell -> {
//                    target.property = source.property
//                }
//                is ComputedCell<*> if target is ComputedCell<*> -> {
//                    target
//                }
//                else -> { }
//            }
//            return target
//        }
        fun <T:PrettyCellBase<P>, P: StylePresets> copyParameters(source:T, target :T):T{
            target.staticModifiers.clear()
            target.staticModifiers.addModifiers(source.staticModifiers.modifiers)
            target.cellOptions = source.cellOptions
            return target
        }
    }
}
