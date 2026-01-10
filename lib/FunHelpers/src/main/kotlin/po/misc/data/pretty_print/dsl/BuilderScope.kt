package po.misc.data.pretty_print.dsl

import po.misc.data.pretty_print.PrettyRow
import po.misc.types.token.TypeToken



interface BuilderScope <V>{
    val type: TypeToken<V>

    fun addRow(row: PrettyRow<V>): PrettyRow<V>

//    @PrettyDSL
//   fun buildRow(
//        rowID: RowID? = null,
//        builder: RowBuilder<V>.()-> Unit
//    ): RowBuilder<V>{
//       val container =  dslEngine.buildRow<V>(type, rowID, builder)
//       val row = container.initRow()
//       addRow(row)
//       return container
//    }

    companion object{
        internal val dslEngine = DSLEngine()
    }

}



