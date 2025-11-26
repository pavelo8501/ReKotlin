package po.misc.data.pretty_print.grid

import po.misc.collections.indexed.IndexedList
import po.misc.collections.indexed.indexedListOf
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.CellReceiverContainer
import po.misc.data.pretty_print.rows.ListContainingRow
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.PrettyRowBase
import po.misc.data.pretty_print.rows.TransitionRow
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class PrettyGrid<T: Any>(
    val typeToken :TypeToken<T>
) {

    internal val prettyRowsBacking: IndexedList<PrettyRowBase> = indexedListOf()
    val prettyRows: List<PrettyRowBase> get() = prettyRowsBacking

    fun addRow(newRow: PrettyRowBase): PrettyGrid<T> {
        prettyRowsBacking.add(newRow)
        return this
    }

    fun buildRow(rowOptions: RowOptions? = null, builder: CellContainer<T>.() -> Unit) {
        val newRow = PrettyRow.buildRow(typeToken, rowOptions, builder)
        addRow(newRow)
    }

    fun buildRow(rowOptions: RowPresets, builder: CellContainer<T>.() -> Unit): Unit =
        buildRow(rowOptions.toOptions(), builder)


    /**
     * Context switch by property
     */
    inline fun <reified T1 : Any> buildRow(
        property: KProperty<T1>,
        preset: RowPresets? = null,
        noinline builder: CellContainer<T1>.() -> Unit
    ) : Unit {
       val row = TransitionRow.buildRow(property, typeToken, preset, builder)
       addRow(row)
    }

    inline fun <reified T1 : Any> buildRow(
        property: KProperty<T1>,
        parentClass: KClass<T>,
        preset: RowPresets? = null,
        noinline builder: CellContainer<T1>.() -> Unit
    ) : Unit {
        val row = TransitionRow.buildRow(property, parentClass, preset, builder)
        addRow(row)
    }

    /**
    * Context switch by List property
    */
    inline fun <reified T1 : Any> buildRows(
        property: KProperty<Collection<T1>>,
        preset: RowPresets? = null,
        noinline builder: CellContainer<Collection<T1>>.() -> Unit
    ) : Unit {
        val listRow = ListContainingRow.buildRow(property, typeToken, preset, builder)
        addRow(listRow)
    }

    fun render(receiver: T): String {
        val stringBuilder = StringBuilder()
        val rowsCount = prettyRows.size
        prettyRows.forEach {row->
            val render = when (row) {
                is PrettyRow -> row.render(receiver)
                is TransitionRow<*> -> {
                    val childReceiver = row.resolveReceiver(receiver)
                    row.render(childReceiver)
                }
                is ListContainingRow<*> -> {
                    val childCollection = row.resolveReceiver(receiver)
                    childCollection.forEach {
                      val render =  row.render(it)
                      stringBuilder.appendLine(render)
                    }
                }
                else -> {

                }
            }
            when{
                row.isFirst  ->  stringBuilder.appendLine(render)
                row.isLast && (rowsCount  <= 2) -> stringBuilder.append(render)
                row.isLast && (rowsCount  > 2) -> stringBuilder.appendLine(render)
                else -> stringBuilder.appendLine(render)
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

inline fun <reified T: Any> T.buildPrettyGrid(builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}






