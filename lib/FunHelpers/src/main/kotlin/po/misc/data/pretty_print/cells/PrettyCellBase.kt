package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.formatters.text_modifiers.TextModifier
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.CommonCellOptions
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.cells.PrettyBorders
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.formatters.TextFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.CellFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.CellStyler
import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.options.PrettyOptions
import po.misc.data.strings.FormattedPair
import po.misc.data.styles.SpecialChars


sealed class PrettyCellBase(var cellOptions: Options){

    var row: PrettyRow<*>? = null
    var index: Int = 0
        internal set

    var keyText: String  = ""
    protected  set(value) {
            if(field.isBlank()) {
                field = value
            }
        }

    private val prettyOptions: PrettyOptions get() =  row?.currentRenderOpt?:cellOptions
    val plainKey: Boolean = prettyOptions.plainKey
    val plainText: Boolean get() =  cellOptions.plainText
    internal val orientation: Orientation get() = row?.currentRenderOpt?.orientation?:Orientation.Horizontal
    internal val renderBorders: Boolean get() = row?.currentRenderOpt?.renderBorders?:false
    internal val cellsCount: Int get() = row?.cells?.size?:1

    var postfix: String? = null

    val borders: PrettyBorders = PrettyBorders()
    val styler : CellStyler = CellStyler(this)
    val cellFormatter: CellFormatter = CellFormatter(this)
    val textFormatter: TextFormatter = TextFormatter(styler, cellFormatter)

    protected fun calculateEffectiveWidth(text: String): Int {
        if (cellOptions.width == 0) {
            return text.length
        }
        val cellWidth = cellOptions.width
        val textLength = text.length
        val align = cellOptions.alignment
        return when(align) {
            Align.CENTER -> {
               (cellWidth - textLength) / 2
            }
            Align.LEFT, Align.RIGHT -> {
                if(textLength > cellWidth){
                    textLength
                }else{
                    cellWidth
                }
            }
        }
    }

    abstract fun copy():PrettyCellBase
    protected fun justifyText(text: String): String {
        val useWidth = calculateEffectiveWidth(text)
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

    protected fun styleKey(): String {
        if(!cellOptions.renderKey){
            return SpecialChars.EMPTY
        }
        if(!cellOptions.plainKey){
            return styler.modify(keyText, cellOptions.keyStyle)
        }
        return keyText
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
        val formatted = textFormatter.style(content)
        val final = justifyText(formatted)
        return final
    }
    open fun render(formatted: FormattedPair, commonOptions: CommonCellOptions?): String {
        applyOptions(PrettyHelper.toOptionsOrNull(commonOptions))
        val usePlain = true
        val usedText = if(usePlain){ formatted.plain.toString() } else { formatted.formatted }
        val formatted = textFormatter.style(usedText)
        val final = justifyText(formatted)
        return final
    }

    companion object
}
