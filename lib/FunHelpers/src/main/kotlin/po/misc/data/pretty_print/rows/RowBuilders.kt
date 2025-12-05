package po.misc.data.pretty_print.rows

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.RowOptions
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1



inline fun <reified T: Any> buildPrettyRow(
    commonOptions: CommonRowOptions? = null,
    noinline builder: CellContainer<T>.()-> Unit
): PrettyRow<T> {
    val token = TypeToken.create<T>()
    val options = PrettyHelper.toRowOptionsOrDefault(commonOptions)
    val container = CellContainer(token, options)
    return  container.buildRow(builder)
}

//
//inline fun <reified T: Any> buildPrettyRow(
//    rowOptions: RowOptions? = null,
//    noinline builder: CellContainer<T>.()-> Unit
//): PrettyRow<T> =  PrettyRow.buildRow(TypeToken.create<T>(), rowOptions,  builder =  builder)






