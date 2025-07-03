package po.misc.data.printable

import po.misc.data.json.JsonHolder


interface Printable {
    fun defaultsToJson(): JsonHolder?
}

