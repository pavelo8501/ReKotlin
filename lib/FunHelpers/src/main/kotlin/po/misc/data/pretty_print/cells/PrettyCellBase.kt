package po.misc.data.pretty_print.cells

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
import po.misc.data.pretty_print.parts.common.BorderSet
import po.misc.data.pretty_print.parts.decorator.Decorator
import po.misc.data.pretty_print.parts.options.CellOptions
import po.misc.data.pretty_print.parts.options.Options
import po.misc.data.pretty_print.parts.render.CellParameters
import po.misc.data.pretty_print.parts.rows.Layout
import po.misc.data.styles.Colour
import po.misc.data.styles.TextStyler
import po.misc.data.text_span.MutablePair
import po.misc.data.text_span.MutableSpan
import po.misc.data.text_span.TextSpan
import po.misc.data.text_span.copyMutable
import po.misc.interfaces.named.NamedComponent
import po.misc.types.token.TypeToken



sealed class PrettyCellBase<T>(
    opts: Options?,
    internal val type: TypeToken<T>
): RenderableCell, TextStyler, NamedComponent, PrettyHelper {

    data class Padding(val left: Int, val right: Int){
        val totalPadding:Int get() = left + right
    }

    var explicitOptions: Boolean = true
        protected set

    var cellOptions: Options = opts?:run {
        explicitOptions = false
        Options()
    }

    override var renderOptions: Options = cellOptions

    protected open val dynamicColour: DynamicColourModifier<*> = DynamicColourModifier(type)
    protected val trimmer: TextTrimmer = TextTrimmer("...")
    protected val styler: CellStyler = CellStyler()

    internal val decorator = Decorator()

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
    var orientation: Orientation = Orientation.Horizontal

    val textFormatter: TextFormatter = TextFormatter(styler, trimmer)
    private var onContentRendered : ((RenderRecord)-> Unit)? = null

    override var verbosity: Verbosity = Verbosity.Warnings

    private val shouldStyle: Boolean get() {
        return !(this is ComputedCell<*, *> && textFormatter.hasDynamic)
    }

    init {
        decorator.addSeparator(cellOptions)
        cellOptions.onUserModified {
            explicitOptions = true
        }
    }

    private fun calculateEffectiveWidth(record: RenderRecord, parameters: CellParameters): Padding {
        if (parameters.layout != Layout.Stretch) {
            return  Padding(0, 0)
        }
        val free = (parameters.availableWidth - record.plainLength).coerceAtLeast(0)
        return when (renderOptions.align) {
            Align.Center -> {
                val left = free / 2
                val right = free - left
                Padding(left, right)
            }
            Align.Left -> Padding(left = 0, right = free)
            Align.Right -> Padding(left = free, right = 0)
        }
    }
    protected fun warn(text: String) {
        text.output(Colour.YellowBright)
    }
    open fun applyOptions(opts: CellOptions?): PrettyCellBase<T>{
        val receivedOptions = toOptionsOrNull(opts)
        if(receivedOptions != null) {
            renderOptions = receivedOptions
            explicitOptions = true
        }
        return this
    }
    abstract override fun copy(): PrettyCellBase<T>

    protected fun justify(record: RenderRecord, parameters: CellParameters?) {
        if (orientation == Orientation.Vertical){
            return
        }
        val padding = if(parameters != null){
            calculateEffectiveWidth(record, parameters)
        }else{
            return
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
    protected fun createKeyed(value: TextSpan, key: String):RenderRecord{
        return RenderRecord(value.copyMutable(), MutablePair(key), renderOptions.keySeparator)
    }

    protected fun finalizeRender(renderRec: RenderRecord): String {
        return with(renderRec){
            when {
                renderOptions.plainText -> {
                    justify(renderRec, null)
                    plain
                }
                renderOptions.sourceFormat -> {
                    justify(renderRec, null)
                    styled
                }
                else ->{
                    if(shouldStyle) {
                        if(shouldStyle){ textFormatter.format(renderRec, renderOptions) }
                        justify(renderRec, null)
                        textFormatter.format(this, renderOptions)
                    }
                    styled
                }
            }
        }
    }
    protected fun CellParameters.finalizeScopedRender(renderRec: RenderRecord): MutableSpan{
        val record =  when {
            renderOptions.plainText -> {
                justify(renderRec, this)
                renderRec
            }
            renderOptions.sourceFormat -> {
                justify(renderRec, this)
                renderRec
            }
            else->{
                if(shouldStyle){ textFormatter.format(renderRec, renderOptions) }
                textFormatter.format(renderRec, this)
                justify(renderRec, this)
                renderRec
            }
        }
        onContentRendered?.invoke(record)
        if(decorator.enabled){
           val decorationContent = decorator.decorate(record)
           return decorationContent.layer
        }
        return record
    }
    protected fun setOptions(opts: Options) {
        cellOptions = opts
        explicitOptions = true
        renderOptions = opts
    }

    fun onContentRendered(callback:(RenderRecord)->Unit){
        onContentRendered = callback
    }
    fun borders(borderSet: BorderSet){
        decorator.addSeparator(borderSet)
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
        val rec =  RenderRecord(MutablePair(content), null, null)
        return finalizeRender(rec)
    }
    companion object
}
