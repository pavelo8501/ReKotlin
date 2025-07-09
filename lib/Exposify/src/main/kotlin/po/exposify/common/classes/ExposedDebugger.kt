package po.exposify.common.classes

import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.classes.notification.models.LogData
import po.lognotify.debug.DebugProxy
import po.lognotify.debug.models.DebugParams
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableContext

class ExposifyDebugger<T: IdentifiableContext, P: PrintableBase<P>>(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    val  dataProcessor: LoggerDataProcessor,
    dataProvider: (DebugParams<P>) -> P
): DebugProxy<T, P>(receiver, printableClass, dataProcessor, dataProvider), SqlLogger {

    override fun log(context: StatementContext, transaction: Transaction) {
        val sqlText = context.expandArgs(transaction)
        super<DebugProxy>.notify(sqlText)
    }

    fun warn(message: String): LogData = dataProcessor.warn(message)
}

fun <T: IdentifiableContext, P: PrintableBase<P>> TasksManaged.exposifyDebugger(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    usingTemplate: PrintableTemplate<P>? = null,
    dataProvider: (DebugParams<P>)-> P
):ExposifyDebugger<T, P>{
    val dataProcessor = this.logHandler.dispatcher.getActiveDataProcessor()
    val proxy = ExposifyDebugger(receiver,printableClass, dataProcessor,  dataProvider)
    proxy.activeTemplate = usingTemplate
    return  proxy
}

