package po.misc.data.printable.json

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion

interface JsonReady<T: PrintableBase<T>> {
    val companion: PrintableCompanion<T>
}

fun <T> T.toJson(): String where  T : PrintableBase<T>, T: JsonReady<*>{
    return companion.jsonDescriptor?.jsonArray?.toJson(this) ?:"[]"
}