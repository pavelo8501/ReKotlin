package po.misc.data.pretty_print.cells

import po.misc.data.Named
import po.misc.data.NamedComponent
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.formatters.FormatterPlugin
import po.misc.data.pretty_print.parts.options.Align
import po.misc.data.pretty_print.formatters.text_modifiers.DynamicColourModifier
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.formatters.TextFormatter
import po.misc.data.pretty_print.formatters.text_modifiers.CellStyler
import po.misc.data.pretty_print.formatters.text_modifiers.ColourCondition
import po.misc.data.pretty_print.formatters.text_modifiers.TextTrimmer
import po.misc.data.pretty_print.parts.cells.RenderRecord
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.rendering.CellRenderNode
import po.misc.data.pretty_print.parts.rendering.CellRenderParameters
import po.misc.data.pretty_print.parts.rendering.RenderParameters
import po.misc.data.pretty_print.parts.rows.RowLayout
import po.misc.data.strings.EditablePair
import po.misc.data.strings.FormattedText
import po.misc.data.strings.createFormatted
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.types.token.TypeToken


data class Padding(
    val left: Int,
    val right: Int,
){
    val totalPadding:Int get() = left + right
}

sealed class PrettyCellBase<T>(
    var cellOptions: Options,
    internal val type: TypeToken<T>
): RenderableCell<T>, TextStyler,  NamedComponent {

    enum class KeyValueTags : Named{ Key, Value }

    var areOptionsExplicit: Boolean = false
        protected set

    override var currentRenderOpts: Options = cellOptions
        protected set

    protected open val dynamicColour: DynamicColourModifier<*> = DynamicColourModifier(type)
    protected val trimmer: TextTrimmer = TextTrimmer("...")
    protected val styler: CellStyler = CellStyler()

    override var keyText: String? = null
        protected set(value) {
            if (field == null) {
                field = value
            }
        }

    override val name: String
        get() = when (this) {
            is StaticCell -> "StaticCell"
            is PrettyCell -> "PrettyCell"
            is KeyedCell<*> -> "KeyedCell"
            is ComputedCell<*, *> -> "ComputedCell"
        }

    var postfix: String? = null


    var parameters: CellRenderParameters? = null

    val orientation: Orientation get() = parameters?.orientation?: Orientation.Horizontal

    val textFormatter: TextFormatter = TextFormatter(styler, trimmer)
    var verbosity: Verbosity = Verbosity.Warnings

    private val shouldStyle: Boolean get() {
        return !(this is ComputedCell<*, *> && textFormatter.hasDynamic)
    }

    init {
        cellOptions.onUserModified {
            areOptionsExplicit = true
        }
    }

    private fun calculateEffectiveWidth(record: RenderRecord): Padding {
        val useWidth = if(currentRenderOpts.width != 0){
            currentRenderOpts.width
        }else{
            record.totalPlainLength
        }
       return when (currentRenderOpts.align) {
            Align.Center ->{
                val diff = useWidth - record.totalPlainLength - keySegmentSize
                val leftPadding = (diff / 2  + keySegmentSize).coerceAtLeast(0)
                val rightPadding = (diff - leftPadding).coerceAtLeast(0)
                Padding(leftPadding, rightPadding)
            }
            Align.Left ->{
                Padding(left = 0, right = useWidth - record.totalPlainLength - keySegmentSize)
            }
            Align.Right ->{
                Padding(left = useWidth - record.totalPlainLength - keySegmentSize, right = 0)
            }
        }
    }

    private fun calculateEffectiveWidth(record: RenderRecord, parameters: RenderParameters): Padding {
        return if (parameters.layout == RowLayout.Stretch) {

            val occupied = record.totalPlainLength + (parameters.projectedSize - keySegmentSize)
            val free = (parameters.width - occupied).coerceAtLeast(0)
            when (currentRenderOpts.align) {
                Align.Center -> {
                    val left = free / 2
                    val right = free - left
                    Padding(left, right)
                }
                Align.Left -> Padding(left = 0, right = free)
                Align.Right -> Padding(left = free, right = 0)
            }
        } else {
            Padding(0, 0)
        }
    }

    protected fun warn(text: String) {
        text.output(Colour.YellowBright)
    }
    abstract fun applyOptions(opts: CellOptions?): PrettyCellBase<T>
    abstract fun copy(): PrettyCellBase<T>

    protected fun justify(record: RenderRecord) {
        if (orientation == Orientation.Vertical){
            return
        }
        val renderParameters = parameters
        val padding = if(renderParameters != null){
            calculateEffectiveWidth(record, renderParameters)
        }else{
            calculateEffectiveWidth(record)
        }
        if(padding.totalPadding == 0){ return }

        val useAlignment = cellOptions.align
        val useFiller = cellOptions.spaceFiller
        when (useAlignment) {
            Align.Left ->{
                val padding = useFiller.toString().repeat(padding.right)
                record.append(padding)
            }
            Align.Right -> {
                val padding = useFiller.toString().repeat(padding.left)
                record.prepend(padding)
            }
            Align.Center -> {
                val paddingLeft = useFiller.toString().repeat(padding.left)
                val paddingRight = useFiller.toString().repeat(padding.right)
                record.prepend(paddingLeft)
                record.append(paddingRight)
            }
        }
    }

    private fun renderWithParameters(renderRec: RenderRecord, parameters: CellRenderParameters):String{
        val key = keyText
        if (key != null) {
            renderRec.addKey(KeyValueTags.Key.createFormatted(key), currentRenderOpts.keySeparator)
        }
        return  when {
            currentRenderOpts.plainText -> {
                parameters.renderComplete(renderRec)
                justify(renderRec)
                renderRec.plain
            }
            currentRenderOpts.sourceFormat -> {
                parameters.renderComplete(renderRec)
                justify(renderRec)
                renderRec.formatted
            }
            else->{
                parameters.measureWidth(renderRec)
                if(shouldStyle){ textFormatter.format(renderRec, currentRenderOpts) }
                textFormatter.format(renderRec, parameters)
                justify(renderRec)
                parameters.renderComplete(renderRec)
                renderRec.formatted
            }
        }
    }

    private fun renderNoParameters(renderRec: RenderRecord):String{
        val key = keyText
        if (key != null) {
            renderRec.addKey(KeyValueTags.Key.createFormatted(key), currentRenderOpts.keySeparator)
        }
        return with(renderRec){
            when {
                currentRenderOpts.plainText -> {
                    justify(renderRec)
                    plain
                }
                currentRenderOpts.sourceFormat -> {
                    justify(renderRec)
                    formatted
                }
                else ->{
                    if(shouldStyle) {
                        if(shouldStyle){ textFormatter.format(renderRec, currentRenderOpts) }
                        justify(renderRec)
                        textFormatter.format(this, currentRenderOpts)
                    }
                    formatted
                }
            }
        }
    }
    internal fun finalizeRender(renderRec: RenderRecord): String {
        val renderParameters = parameters
        return if(renderParameters != null){
            renderWithParameters(renderRec, renderParameters)
        }else{
            renderNoParameters(renderRec)
        }
    }

    override fun parametrizeRender(renderParameters: CellRenderParameters) {
        parameters = renderParameters
    }

    protected fun setOptions(opts: Options) {
        cellOptions = opts
        areOptionsExplicit = true
        currentRenderOpts = opts
    }

    /**
     * Adds one or more static [FormatterPlugin] instances to this cell.
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
    fun addModifiers(vararg modifiers: FormatterPlugin) {
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
    fun colourConditions(builder: DynamicColourModifier<String>.() -> Unit): PrettyCellBase<T> {
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
    fun colourConditions(vararg conditions: ColourCondition<String>) {
        val dynamicColourModifier = DynamicColourModifier(*conditions)
        textFormatter.addFormatter(dynamicColourModifier)
    }

    open fun render(content: String, opts: CellOptions? = null): String {
        applyOptions(PrettyHelper.toOptionsOrNull(opts))
        val rec =  RenderRecord(FormattedText(content))
        return renderNoParameters(rec)
    }

    companion object
}
