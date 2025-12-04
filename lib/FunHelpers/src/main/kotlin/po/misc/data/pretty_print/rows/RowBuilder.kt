package po.misc.data.pretty_print.rows

import po.misc.collections.indexed.IndexedList
import po.misc.collections.indexed.indexedListOf
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.parts.RowPresets
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty


interface RowBuilder<T: Any>{
    val renderBlocks: List<RenderableElement<*, *>>
    fun addRenderBlock(newRow: RenderableElement<*, *>): RowBuilder<T>
    fun buildRow(rowOptions: RowOptions? = null, builder: CellContainer<T>.() -> Unit)
    fun buildRow(rowPresets: RowPresets, builder: CellContainer<T>.() -> Unit): Unit
    fun onRenderElementComplete(callback: (RenderableElement<T, *>)-> Unit)
}

class RowBuilderClass<T: Any>(
    val  typeToken: TypeToken<T>
):RowBuilder<T>{

    internal val prettyRowsBacking: IndexedList<PrettyRow<*>> = indexedListOf()
    internal val renderBlocsBacking = mutableListOf<RenderableElement<*, *>>()

    override val renderBlocks: List<RenderableElement<*, *>> get() = renderBlocsBacking

    @PublishedApi
    internal var onRenderElementCallback: ((RenderableElement<T, *>)-> Unit)? = null
    override fun onRenderElementComplete(callback: (RenderableElement<T, *>)-> Unit){
        onRenderElementCallback = callback
    }


    /**
     * Adds a fully constructed row to this grid.
     *
     * @param newRow a row instance to append
     * @return this grid instance for chaining
     */
    override fun addRenderBlock(newRow: RenderableElement<*, *>): RowBuilder<T> {
        if (newRow is PrettyRow) {
            prettyRowsBacking.add(newRow)
        }
        renderBlocsBacking.add(newRow)
        return this
    }


    /**
     * Builds a new row using a DSL-style builder.
     *
     * @param rowOptions configuration for orientation, styling, and identification
     * @param builder the lambda that defines this row's cells
     */
    override fun buildRow(rowOptions: RowOptions?, builder: CellContainer<T>.() -> Unit) {
        val newRow = PrettyRow.buildRow(typeToken, rowOptions, builder)
        addRenderBlock(newRow)
        renderBlocsBacking.add(newRow)
    }

    /**
     * Builds a row from a preset row configuration.
     *
     * @param rowOptions a preset that can be converted into [RowOptions]
     * @param builder the lambda describing cell contents
     */
    override fun buildRow(rowPresets: RowPresets, builder: CellContainer<T>.() -> Unit): Unit =
        buildRow(rowPresets.toOptions(), builder)

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
        onRenderElementCallback?.invoke(row)
        //gridBase.addRenderBlock(row)
    }

}
