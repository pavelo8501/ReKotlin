package po.misc.data.logging.processor

import po.misc.data.processors.SeverityLevel


interface LogMeta {

    val className: String
    val classID: Long
    val methodName: String?
    val time: String
    val severity: SeverityLevel

}