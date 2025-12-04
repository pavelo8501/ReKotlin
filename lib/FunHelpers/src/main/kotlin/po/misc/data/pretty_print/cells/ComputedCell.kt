package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.CellOptions
import po.misc.data.pretty_print.presets.KeyedPresets
import po.misc.types.token.TypeToken
import kotlin.reflect.KProperty1


class ComputedCell<T: Any>(
    options: CellOptions = CellOptions(),
    var property: KProperty1<Any, T>? = null,
    var lambda: (ComputedCell<T>.(T)-> Any)? = null
): PrettyCellBase<KeyedPresets>(options), KeyedCellRenderer  {

    constructor(
        property:  KProperty1<Any, T>,
        lambda: ComputedCell<T>.(T)-> Any
    ):this(CellOptions(0), property, lambda)

    constructor(
        lambda: ComputedCell<T>.(T)-> Any
    ):this(CellOptions(0), null, lambda)


    var foreignToken: TypeToken<*>? = null
    var triggerOnForeignReceiver: Boolean = false


    fun setForeignToken(token : TypeToken<*>):ComputedCell<T>{
        foreignToken =token
        triggerOnForeignReceiver =true
        return this
    }


    companion object
}
