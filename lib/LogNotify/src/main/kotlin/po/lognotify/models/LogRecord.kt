package po.lognotify.models

import po.misc.data.processors.SeverityLevel


data class LogRecord(
    val task: String,
    val message: String,
    val severity: SeverityLevel = SeverityLevel.INFO,
    val timestamp: Long = System.currentTimeMillis()
){
    var childRecords  = mutableListOf<LogRecord>()

}