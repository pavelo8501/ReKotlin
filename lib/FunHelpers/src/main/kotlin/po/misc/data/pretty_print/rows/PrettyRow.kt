package po.misc.data.pretty_print.rows

import po.misc.collections.asList
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.cells.KeyedCell
import po.misc.data.pretty_print.cells.PrettyCellBase
import po.misc.data.pretty_print.parts.Console220
import po.misc.data.pretty_print.parts.ReceiverListLoader
import po.misc.data.pretty_print.parts.ReceiverLoader
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken


/**
 * A standard pretty-printing row.
 *
 * This row uses the receiver object passed during rendering and renders its
 * cells directly against that object (no nesting or list expansion).
 *
 * @param cells the cells that form this row
 * @param id optional identifier for selective rendering
 */
class PrettyRow<T: Any>(
     typeToken: TypeToken<T>,
    cells: List<PrettyCellBase<*>> = emptyList(),
    options: RowOptions = RowOptions(Console220),
): PrettyRowBase<T>(typeToken, cells,options), RenderableElement<T, T>{

    constructor(container: CellContainerBase<T>):this(container.typeToken,  container.cells,  container.options)

    val loader: ReceiverLoader<T> = ReceiverLoader()

    var listLoader : ReceiverListLoader<*, T>? = null

    override val ids: List<Enum<*>> get() = options.id?.asList()?:emptyList()

    override fun resolveReceiver(parent: T): T = parent

    override fun toString(): String {
        val prettyCells = "PrettyCells: ${prettyCells.size}"
        val keyedCells = "PrettyCells: ${keyedCells.size}"
        val staticCells = "PrettyCells: ${staticCells.size}"
        return "PrettyRow[Total: ${cells.size},$prettyCells,$staticCells, $keyedCells ]"
    }

    companion object{
        operator fun invoke(vararg cells: PrettyCellBase<*>):PrettyRow<String>{
            val  typeToken: TypeToken<String> = TypeToken.create()
            return PrettyRow(typeToken, cells.toList())
        }

        operator fun invoke(cells: List<PrettyCellBase<*>>):PrettyRow<String>{
            val  typeToken: TypeToken<String> = TypeToken.create()
            return PrettyRow(typeToken, cells)
        }

        inline operator fun <reified T: Any> invoke(vararg cells: KeyedCell):PrettyRow<T>{
            val  typeToken: TypeToken<T> = TypeToken.create()
            return PrettyRow(typeToken, cells.toList())
        }

        @PublishedApi
        internal fun <T : Any> buildRow(
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellContainer<T>.() -> Unit
        ): PrettyRow<T> {
            val constructor = CellContainer<T>(token)
            builder.invoke(constructor)
            val realRow = PrettyRow(constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }

        @PublishedApi
        internal fun <T : Any> buildRowForContext(
            receiver: T,
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellReceiverContainer<T>.(T) -> Unit
        ): PrettyRow<T> {
            val constructor = CellReceiverContainer<T>(receiver, token)
            builder.invoke(constructor, receiver)
            val realRow = PrettyRow(constructor)
            if (rowOptions != null) {
                realRow.options = rowOptions
            }
            return realRow
        }
    }
}
