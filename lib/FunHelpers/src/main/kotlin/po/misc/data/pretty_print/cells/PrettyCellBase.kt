package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.formatters.TextFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.CellFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.CellStyler
import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.pretty_print.parts.Options
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.TextStyler

sealed class PrettyCellBase(
    var cellOptions: Options,
){

    open var row: PrettyRow<*>? = null
    private val cellsCount: Int get() = row?.size?:1
    var index: Int = 0
        internal set

    val orientation: Orientation get() = row?.options?.orientation?: Orientation.Horizontal
    val plainText: Boolean get() = row?.options?.plainText?: cellOptions.plainText

    var postfix: String? = null

    //val staticModifiers: StaticModifiers = StaticModifiers()

    val borders: PrettyBorders = PrettyBorders('|', '|')
    val styler : CellStyler = CellStyler(this)
    val cellFormatter: CellFormatter = CellFormatter(this)
    val textFormatter: TextFormatter = TextFormatter(styler, cellFormatter)

//    val textFormatter: DynamicTextFormatter = DynamicTextFormatter{ text, cell, ->
//        if(postfix != null){
//            "$text$postfix"
//        }else{
//            text
//        }
//    }

//    val dynamicTextStyler: DynamicTextStyler = DynamicTextStyler{ text, cell, ->
//        val useStyle = cellOptions.style.textStyle
//        val useColour = cellOptions.style.colour
//        val useBackgroundColour = cellOptions.style.backgroundColour
//        TextStyler.style(text, applyColourIfExists = false, useStyle, useColour, useBackgroundColour)
//    }

   // val compositeFormatter: CompositeFormatter = CompositeFormatter(textFormatter, dynamicTextStyler)

    private fun calculateEffectiveWidth(renderOptions: CellOptions): Int{
        val cellWidth = cellOptions.width

        val rowMaxWidth = row?.rowMaxWidth?: cellWidth
        val align = cellOptions.alignment
        val cellAverageWidth: Int = rowMaxWidth / cellsCount
        if(cellWidth != 0 ){
            return cellWidth
        }
        return  when {
            align == Align.CENTER -> cellAverageWidth
            align != Align.CENTER -> 0
            else -> 0
        }
    }

    protected fun justifyText(text: String, renderOptions: CellOptions): String {
        val useWidth = calculateEffectiveWidth(renderOptions)
        val useAlignment = cellOptions.alignment
        val useFiller = cellOptions.spaceFiller
        return if (orientation == Orientation.Vertical) {
            text
        } else {
            val aligned = when (useAlignment) {
                Align.LEFT -> text.padEnd(useWidth, useFiller)
                Align.RIGHT -> text.padStart(useWidth, useFiller)
                Align.CENTER -> {
                    val fillerString = useFiller.toString()
                    val diff = useWidth - text.length
                    val left = (diff / 2).coerceAtLeast(0)
                    val right = (diff - left).coerceAtLeast(0)
                    fillerString.repeat(left) + text + fillerString.repeat(right)
                }
            }
            borders.render(aligned, this)
        }
    }

    internal fun setRow(prettyRow: PrettyRow<*>): PrettyCellBase{
        row = prettyRow
        return this
    }

    open fun applyOptions(commonOpt: CommonCellOptions?): PrettyCellBase{
        val options = PrettyHelper.toOptionsOrNull(commonOpt)
        if(options != null){
            cellOptions = options
        }
        return this
    }

    /**
     * Adds one or more static [TextModifier] instances to this cell.
     *
     * These modifiers are always applied during rendering, regardless of the
     * cell’s content. They are appended to the internal modifier list in the
     * order provided.
     *
     * Example:
     * ```
     * cell.addModifiers(
     *     BoldModifier(),
     *     UnderlineModifier()
     * )
     * ```
     * @param modifiers modifiers to attach to this cell
     */
    fun addModifiers(vararg modifiers: TextModifier){
        textFormatter.addFormatters(modifiers.toList())
    }

    /**
     * Builds and attaches a [DynamicColourModifier] using a declarative condition DSL.
     *
     * This is the primary way to define *conditional colour rules* for a cell.
     * The builder receives a fresh [DynamicColourModifier] instance, allowing
     * conditions to be declared via the `Colour.buildCondition { … }` DSL.
     *
     * Example:
     * ```
     * cell.colourConditions {
     *     Colour.Green.buildCondition { contains("OK") }
     *     Colour.Red.buildCondition { contains("ERROR") }
     * }
     * ```
     *
     * Behaviour notes:
     * - The modifier is added to this cell's static modifier list.
     * - Conditions are evaluated **in the order they were declared**.
     * - The first matching condition determines the colour.
     *
     * @param builder DSL block used to configure the dynamic colour modifier.
     * @return this cell for fluent chaining.
     */
    fun colourConditions(builder: DynamicColourModifier<String>.()-> Unit):PrettyCellBase{
        val dynamicColourModifier = DynamicColourModifier<String>()
        dynamicColourModifier.builder()
        textFormatter.addFormatter(dynamicColourModifier)
        return this
    }

    /**
     * Adds a [DynamicColourModifier] created from one or more pre-built
     * [ColourCondition] objects.
     *
     * This overload is useful when conditions are constructed elsewhere and reused:
     *
     * ```
     * val warn = DynamicColourCondition(Colour.Yellow) { contains("WARN") }
     * val err  = DynamicColourCondition(Colour.Red)    { contains("ERROR") }
     *
     * cell.colourConditions(warn, err)
     * ```
     * @param conditions the dynamic colour conditions to attach
     */
    fun colourConditions(vararg  conditions : ColourCondition<String>){
        val dynamicColourModifier = DynamicColourModifier(*conditions)
        textFormatter.addFormatter(dynamicColourModifier)
    }

    open fun render(content: String, commonOptions: CommonCellOptions? = null): String {
        applyOptions(PrettyHelper.toOptionsOrNull(commonOptions))
       // val modified =  staticModifiers.modify(content)
       // val formatted =  compositeFormatter.format(modified, this)
        val formatted = textFormatter.style(content)
        val final = justifyText(formatted,  cellOptions)
        return final
    }
    open fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String {
        applyOptions(PrettyHelper.toOptionsOrNull(commonOptions))
        val usePlain = true
        val options = PrettyHelper.toOptions(commonOptions, PrettyHelper.toOptions(cellOptions))
        val usedText = if(usePlain){ formatted.text } else { formatted.formatedText }
       // val modified =  staticModifiers.modify(usedText)
       // val formatted =  compositeFormatter.format(modified, this)
        val formatted = textFormatter.style(usedText)
        val final = justifyText(formatted,  options)
        return final
    }

    companion object
}
