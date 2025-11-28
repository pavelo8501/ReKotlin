package po.misc.data.pretty_print.grid

import po.misc.collections.indexed.IndexedList
import po.misc.collections.indexed.indexedListOf
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.ListContainingRow
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.PrettyRowBase
import po.misc.data.pretty_print.rows.TransitionRow
import po.misc.data.pretty_print.section.PrettySection
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty

/**
 * A container for building and rendering a pretty-printing grid.
 *
 * `PrettyGrid` represents a collection of rows that can render an object of type [T]
 * into human-readable formatted text. Rows may be:
 *
 * - regular rows (`PrettyRow`)
 * - transition rows (`TransitionRow`) used for nested rendering via templates
 * - list rows (`ListContainingRow`) used to render collections
 *
 * The grid supports:
 * - declarative DSL building (`buildRow`, `buildRows`)
 * - row identification through enum IDs
 * - template embedding via `useTemplate`
 * - selective rendering via [RenderOptions]
 *
 * @param T the receiver type used when rendering this grid.
 * @property typeToken runtime type information used for reflective cell rendering.
 */
class PrettyGrid<T: Any>(
    override val typeToken :TypeToken<T>,
) : PrettySection<T>{

    internal val prettyRowsBacking: IndexedList<PrettyRowBase> = indexedListOf()

    /**
     * Public read-only view of all rows in the grid.
     *
     * Rows appear in the same order they were added or constructed via the DSL.
     */
    override val prettyRows: List<PrettyRowBase> get() = prettyRowsBacking

    /**
     * Identification tag for the grid.
     *
     * Assigning this also propagates the identifier to every row in the grid.
     * This enables selective rendering using [RenderOptions].
     */
    override var identification: Enum<*>? = null
        set(value) {
            field = value
            prettyRows.forEach {
                it.id = value
            }
        }

    /**
     * Adds a fully constructed row to this grid.
     *
     * @param newRow a row instance to append
     * @return this grid instance for chaining
     */
    fun addRow(newRow: PrettyRowBase): PrettyGrid<T> {
        prettyRowsBacking.add(newRow)
        return this
    }

    /**
     * Builds a new row using a DSL-style builder.
     *
     * @param rowOptions configuration for orientation, styling, and identification
     * @param builder the lambda that defines this row's cells
     */
    fun buildRow(rowOptions: RowOptions? = null, builder: CellContainer<T>.() -> Unit) {
        val newRow = PrettyRow.buildRow(typeToken, rowOptions, builder)
        addRow(newRow)
    }

    /**
     * Builds a row from a preset row configuration.
     *
     * @param rowOptions a preset that can be converted into [RowOptions]
     * @param builder the lambda describing cell contents
     */
    fun buildRow(rowOptions: RowPresets, builder: CellContainer<T>.() -> Unit): Unit =
        buildRow(rowOptions.toOptions(), builder)

    /**
     * Builds a transition row bound to a property of [T].
     *
     * The row receives the property's value and renders it using the given DSL builder.
     *
     * Example:
     * ```
     * buildRow(User::address) {
     *     addCell(Address::street)
     *     addCell(Address::city)
     * }
     * ```
     *
     * @param property the property whose value will become the row's receiver object
     * @param preset optional row preset for styling and orientation
     * @param builder DSL for defining cells
     */
    inline fun <reified T1 : Any> buildRow(
        property: KProperty<T1>,
        preset: RowPresets? = null,
        noinline builder: CellContainer<T1>.() -> Unit
    ) : Unit {
       val row = TransitionRow.buildRow(property, typeToken, preset, builder)
       addRow(row)
    }


    /**
     * Builds a row that renders every element inside a collection property.
     *
     * Example:
     * ```
     * buildRows(User::orders) {
     *     addCell(Order::id)
     *     addCell(Order::amount)
     * }
     * ```
     *
     * @param property collection property whose elements will each be rendered once
     * @param preset optional styling preset
     * @param builder row cell builder executed for each list element
     */
    inline fun <reified T1 : Any> buildRows(
        property: KProperty<Collection<T1>>,
        preset: RowPresets? = null,
        noinline builder: CellContainer<T1>.() -> Unit
    ) : Unit {
        val listRow = ListContainingRow.buildRow(property, typeToken, preset, builder)
        addRow(listRow)
    }

    /**
     * Incorporates all rows from the given [template] into this grid, configuring them
     * to render against a single receiver provided by [receiverProvider].
     *
     * This is the single-value counterpart of [useTemplateForList], allowing a section
     * template to be reused when rendering is bound to one specific child object.
     *
     * Each row from the template is wrapped into a [TransitionRow], preserving:
     * - all original cells
     * - the row identification (if present)
     * - the row rendering options
     *
     * During rendering, each created [TransitionRow] will:
     * 1. invoke [receiverProvider] to obtain the child receiver,
     * 2. render the row using that receiver.
     *
     * @param T1 The type of the child receiver.
     * @param template The section whose rows should be reused.
     * @param receiverProvider A lambda returning the receiver instance that the template applies to.
     */
    fun <T1 : Any> useTemplate(template: PrettySection<T1>, receiverProvider: ()-> T1 ){
        template.prettyRows.forEach {row ->
            val transitionRow = TransitionRow(template.typeToken, row, receiverProvider)
            transitionRow.options = row.options
            addRow(transitionRow)
        }
    }

    /**
     * Embeds a template grid into this grid as-is.
     *
     * Rows from the template are appended directly and will receive the same receiver [T]
     * during rendering.
     *
     * @param template a compatible pretty-printing template
     */
    fun useTemplate(template: PrettySection<T>){
        template.prettyRows.forEach {row ->
            addRow(row)
        }
    }

    /**
     * Incorporates all rows from the given [template] into this grid, but configures them
     * to render **for each element** of a collection provided by [receiverProvider].
     *
     * This is the list-based variant of [useTemplate], allowing an entire section template
     * to be reused when the data source is a collection rather than a single receiver.
     *
     * Each row from the template is wrapped into a [ListContainingRow], preserving:
     * - the original row’s cells
     * - the row identification (if present)
     * - the row rendering options
     *
     * When this grid is rendered, every created [ListContainingRow] will:
     * 1. invoke [receiverProvider] to obtain a collection of child receivers,
     * 2. render the row once per element in that collection.
     *
     * @param T1 The type of the objects contained in the collection.
     * @param template The section whose rows should be reused.
     * @param receiverProvider A lambda returning a collection of receivers that this template applies to.
     */
    fun <T1 : Any> useTemplateForList(template: PrettySection<T1>, receiverProvider: ()-> Collection<T1>){
        template.prettyRows.forEach {row ->
            val listContainingRow = ListContainingRow(template.typeToken, row.cells,  receiverProvider)
            listContainingRow.options = row.options
            addRow(listContainingRow)
        }
    }

    /**
     * Renders this grid into a formatted multi-line string.
     *
     * Supports selective rendering when [options] is a [RenderOptions]
     * instance. In that case, only rows whose identifiers match entries
     * in `options.renderOnly` are included.
     *
     * For each row type:
     *
     * - `PrettyRow` renders directly against [receiver]
     * - `TransitionRow` obtains a nested receiver before rendering
     * - `ListContainingRow` renders once per element in the collection
     *
     * @param receiver the object used to render each row
     * @param options optional configuration controlling which rows/cells to output
     * @return the formatted string representation of the grid
     */
    override fun render(receiver: T, options: CommonRenderOptions?): String {
        val stringBuilder = StringBuilder()
        for (renderRow in prettyRows){
            val shouldRender =  if(options == null){
                true
            }else{
                if(options !is RenderOptions){
                    true
                }else{
                    renderRow.id in options.renderOnly
                }
            }
            if(!shouldRender) continue
            val printExtraBreakSpace = options?.rowNoGap?:true
            if(!renderRow.isFirst && !printExtraBreakSpace){
                stringBuilder.appendLine("")
            }
            val renderOnlyList = options?.renderOnly?:emptyList()
            when (renderRow){
                is PrettyRow  ->{
                    val render = renderRow.runRender(receiver, rowOptions = null, renderOnlyList)

                    stringBuilder.appendLine(render)
                }
                is TransitionRow<*> -> {
                    val childReceiver = renderRow.resolveReceiver(receiver)
                    val render =  renderRow.runRender(childReceiver, rowOptions = null, renderOnlyList)
                    stringBuilder.appendLine(render)
                }
                is ListContainingRow<*> -> {
                    val childCollection = renderRow.resolveReceiver(receiver)
                    childCollection.forEach {childReceiver ->
                        val render =  renderRow.runRender(childReceiver, rowOptions = null, renderOnlyList)
                        stringBuilder.appendLine(render)
                    }
                }
            }
        }
        return stringBuilder.toString()
    }
}

inline fun <reified T: Any> buildPrettyGrid(builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}

inline fun <reified T: Any> T.buildGridForContext(builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}






