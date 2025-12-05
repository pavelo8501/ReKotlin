package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.stringify
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ComputedCell<T: Any, V: Any>(
    override val typeToken: TypeToken<T>,
    var property: KProperty1<T, V>? = null,
    options: CellOptions = CellOptions(),
    row: PrettyRow<*>? = null,
    var lambda: (ComputedCell<T, V>.(V)-> Any)? = null
): PrettyCellBase<KeyedPresets>(options, row), KeyedCellRenderer, ReceiverAwareCell<T>  {

    constructor(
        typeToken: TypeToken<T>,
        options: CellOptions,
        row: PrettyRow<*>? = null,
        lambda: ComputedCell<T, V>.(V)-> Any
    ):this(typeToken, null, options,  row,  lambda)

    override fun render(receiver: T, commonOptions: CommonCellOptions?): String{
        val value = property?.get(receiver)
        val text = if(value != null){
            val result = lambda?.invoke(this, value)
            result.stringify().formatedText
        }else{
            ""
        }
        val options = PrettyHelper.toCellOptionsOrDefault(commonOptions, cellOptions as CellOptions)
        val modified = staticModifiers.modify(text)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted,  options)
        return final
    }


    companion object
}
