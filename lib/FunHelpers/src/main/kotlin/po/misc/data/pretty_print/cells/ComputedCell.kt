package po.misc.data.pretty_print.cells

import po.misc.data.pretty_print.parts.Align
import po.misc.data.pretty_print.presets.KeyedPresets
import kotlin.reflect.KProperty1


class ComputedCell<T: Any>(
    width: Int,
    var property: KProperty1<Any, T>? = null,
    var lambda: (ComputedCell<T>.(T)-> Any)? = null
): PrettyCellBase<KeyedPresets>(width), KeyedCellRenderer  {


}
