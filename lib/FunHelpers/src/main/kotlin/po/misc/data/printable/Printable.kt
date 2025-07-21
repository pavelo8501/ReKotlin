package po.misc.data.printable

import po.misc.data.json.JsonHolder

interface Printable {
    val formattedString : String
    fun echo()
    fun defaultsToJson(): JsonHolder?
}

