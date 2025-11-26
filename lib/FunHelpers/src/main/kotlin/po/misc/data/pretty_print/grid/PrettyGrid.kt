package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.CellReceiverContainer
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.pretty_print.rows.PrettyRowBase
import po.misc.data.pretty_print.rows.TransitionRow
import po.misc.reflection.Readonly
import po.misc.reflection.resolveTypedProperty
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class PrettyGrid<T: Any>(
    val typeToken :TypeToken<T>
) {

    val prettyRowsBacking: MutableList<PrettyRowBase> = mutableListOf()
    val prettyRows: List<PrettyRowBase> get() = prettyRowsBacking

    private fun isLastRow(index: Int): Boolean{
        return prettyRows.size -1 == index
    }

    fun addRow(newRow: PrettyRowBase): PrettyGrid<T> {
        prettyRowsBacking.add(newRow)
        return this
    }

    fun buildRow(rowOptions: RowOptions? = null, builder: CellContainer<T>.() -> Unit) {
        val newRow = createPrettyRowBuilding(typeToken, rowOptions, builder)
        addRow(newRow)
    }

    fun buildRow(rowOptions: RowPresets, builder: CellContainer<T>.() -> Unit): Unit =
        buildRow(rowOptions.toOptions(), builder)


    inline fun <reified T1 : Any> buildRow(
        switchProperty: KProperty<T1>,
        rowOptions: RowPresets? = null,
        noinline builder: CellContainer<T1>.() -> Unit
    ) {
        val token = TypeToken.create<T1>()
        val options = rowOptions?.toOptions()
        switchProperty.resolveTypedProperty(Readonly, typeToken, token)?.let { kProperty1->
            val newRow = createTransitionRowBuilding(token, kProperty1, options,  builder)
            addRow(newRow)
        } ?: run {
            val errMsg = "switchProperty ${switchProperty.name} can not be resolved on class ${typeToken.simpleName}"
            throw IllegalArgumentException(errMsg)
        }
    }

    fun render(receiver: T): String {
        val stringBuilder = StringBuilder()
        val rowsCount = prettyRows.size
        prettyRows.forEachIndexed {index, row->
            val render = when (row) {
                is PrettyRow -> row.render(receiver)
                is TransitionRow<*> -> {
                    val childReceiver = row.resolveReceiver(receiver)
                    row.render(childReceiver)
                }
            }
            when{

                index == 0 ->  stringBuilder.appendLine(render)
                isLastRow(index) && (rowsCount  <= 2) -> stringBuilder.append(render)
                isLastRow(index) && (rowsCount  > 2) -> stringBuilder.appendLine(render)
                else -> stringBuilder.appendLine(render)
            }
        }
        return stringBuilder.toString()
    }

    companion object {

        @PublishedApi
        internal fun <T : Any> createPrettyRowBuilding(
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellContainer<T>.() -> Unit
        ): PrettyRow {
            val constructor = CellContainer<T>(token)
            builder.invoke(constructor)
            val realRow = PrettyRow(constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }


        @PublishedApi
        internal fun <T : Any> createPrettyRowBuilding(
            receiver: T,
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellReceiverContainer<T>.(T) -> Unit
        ): PrettyRow {
            val constructor = CellReceiverContainer<T>(receiver, token)
            builder.invoke(constructor, receiver)
            val realRow = PrettyRow(constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }

        @PublishedApi
        internal fun <T : Any> createTransitionRowBuilding(
            token: TypeToken<T>,
            switchProperty: KProperty1<Any, T>,
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
    }
}

@Deprecated("Change to buildPrettyGrid")
inline fun <reified T: Any> prettyGrid( builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}


inline fun <reified T: Any> buildPrettyGrid( builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
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






