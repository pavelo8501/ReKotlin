package po.misc.data.pretty_print.grid

import po.misc.data.output.output
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.CommonRenderOptions
import po.misc.data.pretty_print.parts.ReceiverListLoader
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowRender
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.ListContainingRow
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.PrettyRowBase
import po.misc.data.pretty_print.rows.RowBuilder
import po.misc.data.pretty_print.rows.RowBuilderClass
import po.misc.data.pretty_print.rows.TransitionRow
import po.misc.data.pretty_print.section.PrettySection
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.types.safeCast
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


sealed class PrettyGridBase<T: Any>(
    override val typeToken :TypeToken<T>,
    val options: RowOptions,
    val builders: RowBuilder<T> = RowBuilderClass(typeToken)
) :PrettySection<T>, TokenFactory, RowBuilder<T> by  builders  {

    /**
     * Public read-only view of all rows in the grid.
     *
     * Rows appear in the same order they were added or constructed via the DSL.
     */
    override val prettyRows: List<PrettyRow<*>>
        get() {
            val resultingList = mutableListOf<PrettyRow<*>>()
            renderBlocks.forEach { renderBlock ->
                when (renderBlock) {
                    is PrettyRow<*> -> resultingList.add(renderBlock)
                    is PrettyPromiseGrid<*, *> -> {
                        resultingList.addAll(renderBlock.prettyRows)
                    }
                }
            }
            return resultingList
        }

    /**
     * Identification tag for the grid.
     *
     * Assigning this also propagates the identifier to every row in the grid.
     * This enables selective rendering using [RenderOptions].
     */
    override val ids: List<Enum<*>> get() = prettyRows.mapNotNull { it.id }


    protected fun renderParameterOrDefault(renderParam: RowRender?): RowRender {
        return renderParam ?: RowRender(options)
    }

    inline fun <reified T : PrettyRowBase<*>> getRows(): List<T> {
        return prettyRows.filterIsInstance<T>()
    }

    fun <V : Any> getRowsNotOfType(typeToken: TypeToken<V>): List<PrettyRowBase<*>> {
        val result = prettyRows.filter {
            it.typeToken != typeToken
        }
        return result
    }

    inline fun <reified T : RenderableElement<*, *>> getRenderable(): List<T> {
        return renderBlocks.filterIsInstance<T>()
    }

    fun getAllRows(): List<PrettyRowBase<*>> {
        return prettyRows
    }

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
    ): Unit {
        val row = TransitionRow.buildRow(property, typeToken, preset, builder)
        addRenderBlock(row)
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
    inline fun <reified T: Any, reified V : Any> buildRows(
        property: KProperty1<T,  Collection<V>>,
        rowPresets: RowPresets? = null,
        noinline builder: CellContainer<V>.() -> Unit
    ): Unit {
        val token = tokenOf<V>()
        val options = rowPresets?.toOptions()?:options
        val row = PrettyRow.buildRow(token, options, builder)
        row.listLoader =  ReceiverListLoader(token, property)
        addRenderBlock(row)
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
    fun <T1 : Any> useTemplate(template: PrettySection<T1>, receiverProvider: () -> T1) {
        template.prettyRows.forEach { row ->
            val transitionRow = TransitionRow<T, T1>(template.typeToken, row, receiverProvider)
            addRenderBlock(transitionRow)
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
    fun useTemplate(template: PrettySection<T>) {
        template.prettyRows.forEach { row ->
            addRenderBlock(row)
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
    inline fun <reified T1 : Any> useTemplateForList(
        template: PrettySection<T1>,
        noinline receiverProvider: () -> Collection<T1>
    ) {
        val listContainingRow = ListContainingRow(template.typeToken, template, receiverProvider)
        addRenderBlock(listContainingRow)
    }

    private fun checkShouldRender(row: RenderableElement<*, *>, options: CommonRenderOptions): Boolean {
        return if (options !is RowRender) {
            true
        } else {
            if (options.renderOnly.isEmpty()) {
                true
            } else {
                row.ids.any { it in options.renderOnly}
            }
        }
    }

    private fun renderRow(row: PrettyRow<*>, receiver: T, renderOptions: RowRender): String{
        val typeChecked = row.safeCast<PrettyRow<T>>()
        if (typeChecked != null) {
            val loader =  typeChecked.listLoader
            if(loader == null){
                return typeChecked.render(receiver, renderOptions)
            }else{
                val result = loader.tryResolveReceiver(receiver)
                if(result != null){
                    return row.renderList(result, renderOptions)
                }
            }
        }else{
            val loader =  row.listLoader
            if(loader != null){
                val result = loader.tryResolveReceiver(receiver)
                if(result != null){
                   return row.render(result)
                }
            }
        }
        return ""
    }
    private fun renderTransitionRow(row: TransitionRow<*, *>, receiver: T, options: RowRender): String {
        val typeChecked = row.safeCast<TransitionRow<T, *>>()
        if (typeChecked == null) {
            "TransitionRow ${row.id?.name ?: row} was not rendered".output(Colour.Yellow)
            return SpecialChars.EMPTY
        }
        return typeChecked.render(receiver, options)
    }
    private fun renderListContainingRow(row: ListContainingRow<*, *>, receiver: T, options: RowRender): String {
        val typeChecked = row.safeCast<ListContainingRow<T, *>>()
        if (typeChecked == null) {
            "TransitionRow ${row.id?.name ?: row} was not rendered".output(Colour.Yellow)
            return SpecialChars.EMPTY
        }
        return typeChecked.render(receiver, options)
    }
    private fun renderPromiseRows(row: PrettyPromiseGrid<*, *>, receiver: T, options: RowRender): String {
        val typeChecked = row.safeCast<PrettyPromiseGrid<T, *>>()
        if (typeChecked == null) {
            "TransitionRow $row was not rendered".output(Colour.Yellow)
            return SpecialChars.EMPTY
        }
        return typeChecked.renderTemplate(receiver, options)
    }

    protected fun renderAll(receiver: T, renderOptions: RowRender?): String {
        val stringBuilder = StringBuilder()
        for (renderBlock in renderBlocks) {
            val useRender = renderParameterOrDefault(renderOptions)
            val shouldRender = checkShouldRender(renderBlock, useRender)
            if (!shouldRender) continue
            val renderedString = when (renderBlock) {
                is PrettyRow -> renderRow(renderBlock, receiver, useRender)
                is TransitionRow<*, *> -> renderTransitionRow(renderBlock, receiver, useRender)
                is ListContainingRow<*, *> -> renderListContainingRow(renderBlock, receiver, useRender)
                is PrettyPromiseGrid<*, *> -> renderPromiseRows(renderBlock, receiver, useRender)
                else -> ""
            }
            stringBuilder.appendLine(renderedString)
        }
        return stringBuilder.toString()
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
    override fun render(receiver: T, renderOptions: RowRender?): String {
       return renderAll(receiver, renderOptions)
    }
}

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
   typeToken :TypeToken<T>,
   options: RowOptions = RowOptions(),
) : PrettyGridBase<T>(typeToken, options){

    fun <T1: Any> useTemplate(promiseGrid: PrettyPromiseGrid<T, T1>){
        addRenderBlock(promiseGrid)
    }
}

/**
 * Represents a reusable, promise-based grid template for rendering a collection property
 * of a receiver type [T].
 *
 * A [PrettyPromiseGrid] does not immediately contain rows. Instead, it stores:
 *  - a [TypeToken] describing the element type [V]
 *  - a reference to the collection property (via [ReceiverListLoader])
 *  - a template builder (`onReceiverAvailable`) that describes how rows and cells
 *    should be generated from the element list
 *
 * When [processReceiver] is called:
 *  1) the loader extracts the collection from the given receiver
 *  2) the template builder is executed on this grid instance
 *  3) rows are produced and accumulated inside the grid
 *
 * This allows you to define a rendering template once and reuse it for any number
 * of receivers of type [T], making it ideal for reusable views, logging tables,
 * reporting, and DSL-driven UI.
 *
 * @param typeToken Reflection token describing the element type.
 * @param property Property on [T] that provides the collection to render.
 * @param options Default row options applied during row creation.
 * @param onReceiverAvailable Template builder invoked once a receiver’s data is available.
 */
class PrettyPromiseGrid<T: Any, V: Any>(
    typeToken :TypeToken<T>,
    val valueToken: TypeToken<V>,
    property: KProperty1<T,  Collection<V>>,
    options: RowOptions = RowOptions(),
    val onReceiverAvailable:  PrettyPromiseGrid<T, V>.(List<V>)-> Unit

) : PrettyGridBase<V>(valueToken, options), RenderableElement<T, List<V>>{

    val loader: ReceiverListLoader<T, V> = ReceiverListLoader(valueToken, property)

    override val ids: List<Enum<*>> get() = prettyRows.mapNotNull { it.id }

    init {
        loader.provideBuilder(onReceiverAvailable)
    }

    /**
     * Builds grid rows for the given list of receiver items.
     *
     * For each element in the list:
     *  - a [CellReceiverContainer] is created
     *  - the caller’s [builder] block defines how cells are added
     *  - a final row is produced via [toPrettyRow] and added to the grid
     *
     * This is the low-level mechanism behind grid row construction.
     *
     * @param receiverList The list of items to be transformed into rows.
     * @param options Row configuration.
     * @param builder Cell construction block.
     */
    fun buildRow(receiverList: List<V>, options: RowOptions, builder: CellContainer<V>.()-> Unit){
        val container =  CellContainer(valueToken, options)
        container.builder()
        val row = PrettyRow(container)
        addRenderBlock(row)
    }

    fun buildRow(receiverList: List<V>, builder: CellContainer<V>.()-> Unit): Unit =
        buildRow(receiverList, options, builder)


    fun buildRenderTemplate(receiver: T): List<PrettyRow<*>>{
        loader.resolveTemplate(receiver, this)
        return prettyRows
    }

    fun renderAsList(receiver: T, renderParam: RowRender? = null): List<String>{
        val resultList = mutableListOf<String>()
        val renderParam = renderParameterOrDefault(renderParam)
        val receivedList =  loader.resolveTemplate(receiver, this)

        prettyRows.forEach { row ->
            if(row.typeToken != typeToken){
               val render =  row.render(receiver)
               resultList.add(render)
            }else{
                val result =  row.render(receiver)
                resultList.add(result)
            }
        }
        return resultList
    }

    override fun render(receiver: V, renderOptions: RowRender?): String{
        return renderAll(receiver, renderOptions)
    }

    fun renderTemplate(receiver: T, renderOptions: RowRender? = null): String{
        val resultList = loader.resolveTemplate(receiver, this)
        return resultList.joinToString(separator = SpecialChars.NEW_LINE) {result  ->
            renderAll(result, renderOptions)
        }
    }

    override fun toString(): String {
       return "PrettyPromiseGrid[Rows: ${prettyRows.size} Render blocks: ${renderBlocks.size}]"
    }

    override fun resolveReceiver(parent: T): List<V> {
        return loader.resolveReceiver(parent)
    }
}









