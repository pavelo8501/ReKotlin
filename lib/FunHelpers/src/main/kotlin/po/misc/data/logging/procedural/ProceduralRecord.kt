package po.misc.data.logging.procedural

import po.misc.data.logging.LogRecord


interface ProceduralRecord: LogRecord {
    fun registerRecord(record: ProceduralEntry)
}
