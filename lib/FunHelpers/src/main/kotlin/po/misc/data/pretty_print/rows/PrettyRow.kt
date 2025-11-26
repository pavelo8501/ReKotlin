package po.misc.data.pretty_print.rows


import po.misc.collections.asList
import po.misc.context.tracable.TraceableContext
import po.misc.counters.LogJournal
import po.misc.data.output.output
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.RenderDefaults
import po.misc.data.pretty_print.cells.ComputedCell
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.cells.StaticCell
import po.misc.data.pretty_print.grid.PrettyGrid.Companion.createTransitionRowBuilding
import po.misc.data.pretty_print.parts.Orientation
import po.misc.data.pretty_print.parts.PrettyBorders
import po.misc.data.pretty_print.parts.RenderOptions
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.stringify
import po.misc.data.styles.SpecialChars
import po.misc.reflection.NameValuePair
import po.misc.reflection.Readonly
import po.misc.reflection.getBrutForced
import po.misc.reflection.resolveTypedProperty
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Represents a row of formatted cells that can render an ordered list of values.
 *
 * Typical usage:
 * ```
 * val row = PrettyRow(
 *     PrettyCell(10, KeyPreset),
 *     PrettyCell(20, ValuePreset)
 * )
 * println(row.render(listOf("Name", "John Doe")))
 * ```
 *
 * Extra values exceeding the number of cells are appended after the row
 */

sealed class PrettyRowBase(
    private var initialCells: List<PrettyCellBase<*>>,
): TraceableContext{

    enum class RendererOperation{ Render, RenderCell }

    var defaults: RenderDefaults = Console220
    var options: RowOptions = RowOptions(Console220)

    val rowBorders : PrettyBorders = PrettyBorders()

    internal val cellsBacking: MutableList<PrettyCellBase<*>> = mutableListOf()
    val cells : List<PrettyCellBase<*>> get() = cellsBacking
    val journal : LogJournal<RendererOperation> = LogJournal(this, RendererOperation.Render)

    init {
        setCells(initialCells)
    }

    private fun selectOption(cell: PrettyCellBase<*>, rowOption: RowOptions): RenderOptions{
        val lastIndex = cells.size - 1
        val index = cell.index
        val orientation = rowOption.orientation
        val usePlain = false

        if(orientation == Orientation.Vertical){
           return RenderOptions(orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        }
        val options = when {
            index == 0 && cells.size == 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == 0 && cells.size > 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)

            index == lastIndex && cells.size == 1 ->  RenderOptions(orientation, usePlain, renderLeftBorder = false, false).assignParameters(this)
            index == lastIndex && cells.size > 1 -> RenderOptions(orientation,usePlain, renderLeftBorder = false, false).assignParameters(this)

            cells.size > 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = false, true).assignParameters(this)
            cells.size <= 1 -> RenderOptions(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)

            else -> RenderOptions(orientation, usePlain, renderLeftBorder = true, true).assignParameters(this)
        }
        return options
    }

    private fun runListRenderLessCells(rowOptions: RowOptions, list: List<Any>, stingBuilder: StringBuilder){
        val cellsCount = cells.size

        for (i in 0 until cellsCount) {
            val cell = cells[i]
            val listItem = list[i]
            val options = selectOption(cell, rowOptions)
            if(rowOptions.orientation == Orientation.Horizontal){
                val value = renderCell(listItem, cell, options)
                stingBuilder.append(value)
            }else{
                val value = renderCell(listItem, cell, options)
                stingBuilder.appendLine(value)
            }
        }
        val tailString =  list.drop(cellsCount).joinToString(" ")

        cells.lastOrNull()?.let {
            val options = selectOption(it, rowOptions)
            val tailResult = it.render(tailString, options)
            stingBuilder.append(tailResult)
        }
    }
    private fun runListRenderCellsMoreOrEqual(rowOptions: RowOptions, list: List<Any>, stingBuilder: StringBuilder) {

        val noBordersOption = RenderOptions(rowOptions.orientation, renderLeftBorder = false, renderRightBorder = false).assignFinalize(this)
        list.forEachIndexed { index, element->
            val selectedCell = cells[index]

            val options = selectOption(selectedCell, rowOptions)

            if(rowOptions.orientation == Orientation.Horizontal){
                val rendered = renderCell(element, selectedCell, options)
                stingBuilder.append(rendered)
            }else{
                val rendered = renderCell(element, selectedCell, noBordersOption)
                stingBuilder.appendLine(rendered)
            }
        }
    }

    private fun <T: Any> renderKeyed(receiver: T, cell: KeyedCell, options: RenderOptions): String{
        val castedProperty =  cell.property.castOrThrow<KProperty1<T, *>>()
        val formatted = castedProperty.get(receiver).stringify()
        val renderResult = cell.render(formatted, options)
        return renderResult
    }
    private fun <T: Any> renderPretty(receiver: T, prettyCell: PrettyCell): String{
        val result = prettyCell.postfix?:""
        return result
    }

    private fun <T: Any>  renderStatic(receiver: T,  cell: StaticCell, options: RenderOptions): String{
        journal.addRecord(RendererOperation.RenderCell, "Static cell with options: $options")
        val renderResult = if(cell.lockContent){
            cell.render(options)
        }else{
            val formatted = receiver.stringify()
            cell.render(formatted, options)
        }
        return renderResult
    }
    private fun <T: Any> renderComputed(receiver: T, cell: ComputedCell<*>, options: RenderOptions): String{
        journal.addRecord(RendererOperation.RenderCell, "Computed cell with options: [$options]")
        val casted = cell.castOrThrow<ComputedCell<T>>()
        val newReceiver = casted.property?.get(receiver)
        if(newReceiver != null){
            val result = casted.lambda?.invoke(casted, newReceiver)
            val formatted = result.stringify()
            val renderResult = cell.render(formatted, options)
            return renderResult
        }
        return ""
    }

    internal fun <T: Any> renderCell(receiver: T, cell: PrettyCellBase<*>, options: RenderOptions): String{
        return  when(cell){
            is ComputedCell<*> -> renderComputed(receiver, cell, options)
            is KeyedCell -> renderKeyed(receiver, cell, options)
            is StaticCell->  renderStatic(receiver, cell, options)
            is PrettyCell -> renderPretty(receiver, cell)
        }
    }

    internal fun <T: Any> runRender(receiver: T, rowPreset: RowPresets? = null): String {
        val useRowOptions = rowPreset?.toOptions(defaults) ?: options
        val cellCount = cells.size
        journal.addRecord("Cells count $cellCount")
        val result = StringBuilder()

        cells.take(cellCount - 1).forEach {cell->
            val options = selectOption(cell, useRowOptions)
            if (useRowOptions.orientation == Orientation.Horizontal) {
                val rendered = renderCell(receiver, cell, options)
                result.append(rendered)
            } else {
                val rendered = renderCell(receiver, cell, options)
                result.appendLine(rendered)
            }
        }
        cells.lastOrNull()?.let {lastCell->
            val options = selectOption(lastCell, useRowOptions)
            val rendered = renderCell(receiver, lastCell, options)
            result.append(rendered)
        }
        return result.toString()
    }

    internal fun <T: Any> runListRender(list: List<T>, rowPreset: RowPresets? = null): String {
        val rowOptions = rowPreset?.toOptions(defaults)
        val optionsToUse = rowOptions?:options
        val cellsCount = cells.size
        journal.addRecord("Cells count $cellsCount")
        val listSize = list.size
        val result = StringBuilder()
        when {
            cellsCount < listSize -> runListRenderLessCells(optionsToUse, list, result)
            cellsCount >= listSize -> runListRenderCellsMoreOrEqual(optionsToUse, list, result)
        }
        return result.toString()
    }

    fun <T: Any> render(receiver: T, rowPreset: RowPresets? = null): String = runRender(receiver, rowPreset)

    fun render(value: Any,  vararg values: Any, rowPreset: RowPresets? = null): String{
        val valuesList = buildList {
            add(value)
            addAll(values.toList())
        }
        return runListRender(valuesList, rowPreset)
    }
    fun render(values: List<String>, rowPreset: RowPresets? = null): String {
        return runListRender(values, rowPreset)
    }
    fun render(nameValuePair: NameValuePair): String = runListRender(listOf(nameValuePair.name, nameValuePair.value))

    fun setCells(newCells: List<PrettyCellBase<*>>){
        cellsBacking.clear()
        newCells.forEachIndexed { index, cell->
            cell.index = index
            cellsBacking.add(cell)
        }
    }
    fun setCells(cell:  PrettyCellBase<*>,  vararg newCells: PrettyCellBase<*>): Unit{
        val newList = buildList {
            add(cell)
            addAll(newCells.toList())
        }
        setCells(newList)
    }


    fun applyPreset(preset: RowPresets): PrettyRowBase{
        options = preset.toOptions()
        return this
    }
}

class PrettyRow(
    initialCells: List<PrettyCellBase<*>>,
): PrettyRowBase(initialCells){

    constructor(vararg cells: PrettyCellBase<*>):this(cells.toList())
    constructor(container: PrettyDataContainer):this(container.prettyCells){
        setCells(container.prettyCells)
    }
}

class TransitionRow<T: Any>(
    override val typeToken: TypeToken<T>,
    initialCells: List<PrettyCellBase<*>> = emptyList()
): PrettyRowBase(initialCells), RenderableElement<T>, TraceableContext {
    constructor(typeToken: TypeToken<T>, vararg cells: PrettyCellBase<*>):this(typeToken, cells.toList())

    constructor(
        token: TypeToken<T>,
        property: KProperty1<Any, T>,
        container: PrettyDataContainer
    ):this(token){
        setCells(container.prettyCells)
        transitionPropertyBacking = property
    }
    constructor(
        token: TypeToken<T>,
        transitionLambda: () -> T,
        container: PrettyDataContainer
    ):this(token){
        setCells(container.prettyCells)
        transitionBacking = transitionLambda
    }

    internal var  transitionPropertyBacking: KProperty1<Any, T>? = null
    val  transitionProperty: KProperty1<Any, T> get() {
        return transitionPropertyBacking.getOrThrow(KProperty1::class)
    }

    internal var transitionBacking: (() -> T)? = null
    val transition: () -> T get() {
       return transitionBacking.getOrThrow(this)
    }

    fun provideTransition(provider: () -> T){
        transitionBacking = provider
    }

    override fun resolveReceiver(parentReceiver:Any):T{
        if(transitionPropertyBacking != null){
            return transitionProperty.getBrutForced(typeToken, parentReceiver)
        }
        return transition.invoke()
    }

    companion object{

        fun <T : Any> buildRow(
            switchProperty: KProperty1<Any, T>,
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellContainer<T>.() -> Unit
        ): TransitionRow<T> {
            val constructor = CellContainer<T>(token)
            builder.invoke(constructor)
            val realRow = TransitionRow<T>(token, switchProperty, constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }

        inline fun <reified T: Any> buildRow(
            switchProperty: KProperty1<Any, T>,
            rowOptions: RowOptions? = null,
            noinline builder: CellContainer<T>.() -> Unit
        ): TransitionRow<T> = buildRow(switchProperty,TypeToken.create<T>(), rowOptions, builder)


        inline fun <reified T : Any> buildRow(
            switchProperty: KProperty<T>,
            parentClass: KClass<*>,
            rowOptions: RowPresets? = null,
            noinline builder: CellContainer<T>.() -> Unit
        ): TransitionRow<T> {
            val token = TypeToken.create<T>()
            val options = rowOptions?.toOptions()
            return switchProperty.resolveTypedProperty(Readonly, parentClass, token)?.let { kProperty1 ->
                buildRow(kProperty1, options, builder)
            }?:run {
                val errMsg = "switchProperty ${switchProperty.name} can not be resolved on class ${parentClass.simpleName}"
                throw IllegalArgumentException(errMsg)
            }
        }
    }
}



