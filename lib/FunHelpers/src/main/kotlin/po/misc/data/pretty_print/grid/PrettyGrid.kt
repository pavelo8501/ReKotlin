package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.RowOptions
import po.misc.data.pretty_print.presets.RowPresets
import po.misc.data.pretty_print.rows.CellContainer
import po.misc.data.pretty_print.rows.CellReceiverContainer
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.types.token.TypeToken

class PrettyGrid<T: Any>(
    val typeToken :TypeToken<T>
) {

    val prettyRows: MutableList<PrettyRow> = mutableListOf()

    fun addRow(newRow : PrettyRow):PrettyGrid<T>{
        prettyRows.add(newRow)
        return this
    }

    fun buildRow(rowOptions: RowOptions? = null,  builder: CellContainer<T>.()-> Unit){
        val newRow = createPrettyRowBuilding(typeToken, rowOptions, builder)
        addRow(newRow)
    }

    fun buildRow(rowOptions: RowPresets,  builder: CellContainer<T>.()-> Unit): Unit = buildRow(rowOptions.toOptions(), builder)

    fun render(receiver: T): String{
        val stringBuilder = StringBuilder()
        for(prettyRow in prettyRows){
           val renderedRow = prettyRow.render(receiver)
            stringBuilder.appendLine(renderedRow)
        }
        return stringBuilder.toString()
    }

    companion object{

       @PublishedApi
       internal fun <T: Any> createPrettyRowBuilding(
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellContainer<T>.()-> Unit
        ): PrettyRow{
            val constructor = CellContainer<T>(token)
            builder.invoke(constructor)
            val realRow = PrettyRow(constructor)
            if(rowOptions != null){
                realRow.options = rowOptions
            }
            return realRow
        }


        @PublishedApi
        internal fun <T: Any> createPrettyRowBuilding(
            receiver:T,
            token: TypeToken<T>,
            rowOptions: RowOptions? = null,
            builder: CellReceiverContainer<T>.(T)-> Unit
        ): PrettyRow {
            val constructor = CellReceiverContainer<T>(receiver, token)
            builder.invoke(constructor, receiver)
            val realRow = PrettyRow(constructor)
            if(rowOptions != null){
                realRow.options = rowOptions
            }
            return realRow
        }

    }
}


inline fun <reified T: Any> prettyGrid( builder: PrettyGrid<T>.() -> Unit):PrettyGrid<T>{
    val token = TypeToken.create<T>()
    val grid = PrettyGrid<T>(token)
    builder.invoke(grid)
    return grid
}

