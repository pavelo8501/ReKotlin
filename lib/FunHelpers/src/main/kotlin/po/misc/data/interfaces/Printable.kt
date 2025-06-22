package po.misc.data.interfaces

import po.misc.data.json.JObject
import po.misc.data.json.JsonHolder


interface Printable {
    fun defaultsToJson(): JsonHolder?
}

interface PrintableProvider<T:Printable> {
    val template: T.()-> String

}
