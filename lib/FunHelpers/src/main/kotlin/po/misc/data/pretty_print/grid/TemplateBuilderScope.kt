package po.misc.data.pretty_print.grid

import po.misc.data.pretty_print.parts.CommonRowOptions
import po.misc.data.pretty_print.parts.PrettyDSL
import po.misc.data.pretty_print.parts.RowOptionsEditor
import po.misc.data.pretty_print.rows.RowBuilderScope
import po.misc.data.pretty_print.rows.RowValueContainer
import po.misc.types.token.TypeToken

@PrettyDSL
sealed interface TemplateBuilderScope<T: Any, V: Any> : RowBuilderScope<V>, RowOptionsEditor{
    val hostType: TypeToken<T>
    override val type: TypeToken<V>

    @PrettyDSL
    fun buildRow(rowOptions: CommonRowOptions? = null, builder: RowValueContainer<T, V>.() -> Unit)
    fun renderHere()

}