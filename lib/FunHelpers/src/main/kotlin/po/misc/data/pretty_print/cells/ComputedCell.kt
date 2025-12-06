package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CommonCellOptions
import po.misc.data.pretty_print.parts.PrettyHelper
import po.misc.data.pretty_print.parts.Options
import po.misc.data.pretty_print.parts.ValueLoader
import po.misc.data.pretty_print.rows.PrettyRow
import po.misc.data.strings.append
import po.misc.data.strings.appendLineParam
import po.misc.data.strings.appendParam
import po.misc.data.strings.stringify
import po.misc.functions.Throwing
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ComputedCell<T: Any, V: Any>(
    override val typeToken: TypeToken<T>,
    valueToken: TypeToken<V>,
    row: PrettyRow<*>? = null,
    property: KProperty1<T, V>?,
    val lambda: ComputedCell<T, V>.(V)-> Any
): PrettyCellBase(Options(), row), ReceiverAwareCell<T> {

    constructor(
        typeToken: TypeToken<T>,
        valueToken: TypeToken<V>,
        row: PrettyRow<*>? = null,
        provider: () -> V,
        lambda: ComputedCell<T, V>.(V) -> Any
    ) : this(typeToken, valueToken, row, property = null, lambda = lambda) {
        singleLoader.setProvider(provider)
    }

    val singleLoader: ValueLoader<T, V> = ValueLoader("ComputedCell", typeToken, valueToken)

    var usePlain: Boolean
        get() = cellOptions.usePlain
        set(value){
            cellOptions.usePlain = value
        }

    init {
        property?.let {
            singleLoader.setReadOnlyProperty(it)
        }
    }

    override fun applyOptions(opt: CommonCellOptions?): ComputedCell<T, V> {
        opt?.let {
            cellOptions = PrettyHelper.toOptions(it)
        }
        return this
    }

    override fun render(receiver: T, commonOptions: CommonCellOptions?): String {
        val value = singleLoader.resolveValue(receiver, Throwing)
        val computed = lambda.invoke(this, value)
        val text = if(cellOptions.usePlain){
            computed.toString()
        }else{
            computed.stringify().formatedString
        }
        val options = PrettyHelper.toOptions(commonOptions, cellOptions as Options)
        val modified = staticModifiers.modify(text)
        val formatted = compositeFormatter.format(modified, this)
        val final = justifyText(formatted, options)
        return final
    }

    override fun toString(): String {
        return buildString {
            append("ComputedCell")
            appendParam(" Id", cellOptions.id)
            appendParam(" Width", cellOptions.width)
        }
    }

    companion object
}
