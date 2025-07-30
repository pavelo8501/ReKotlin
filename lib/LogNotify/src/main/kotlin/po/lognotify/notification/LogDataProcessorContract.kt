package po.lognotify.notification

import po.misc.data.printable.PrintableBase
import po.misc.data.processors.SeverityLevel


interface LogDataProcessorContract {
    fun log(arbitraryRecord: PrintableBase<*>, severity: SeverityLevel, emitter: Any)
    fun notify(message: String, severity: SeverityLevel, emitter: Any)
}