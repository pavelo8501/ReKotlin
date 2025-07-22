package po.exposify.common.classes

import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import po.lognotify.TasksManaged
import po.lognotify.classes.notification.LoggerDataProcessor
import po.lognotify.debug.DebugProxy
import po.lognotify.debug.models.DebugParams
import po.misc.context.CTX
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.data.printable.PrintableTemplateBase

class ExposifyDebugger<T: CTX, P: PrintableBase<P>>(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    val  dataProcessor: LoggerDataProcessor,
    dataProvider: (DebugParams<P>) -> P
): DebugProxy<T, P>(receiver, printableClass, dataProcessor, dataProvider), SqlLogger {

    override fun log(context: StatementContext, transaction: Transaction) {
        val sqlText = context.expandArgs(transaction)
        super<DebugProxy>.notify(sqlText)
    }

    fun warn(message: String) = dataProcessor.warn(message)
}

fun <T: CTX, P: PrintableBase<P>> TasksManaged.exposifyDebugger(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    usingTemplate: PrintableTemplateBase<P>? = null,
    dataProvider: (DebugParams<P>)-> P
):ExposifyDebugger<T, P>{
    val dataProcessor = this.logHandler.dispatcher.getActiveDataProcessor()
    val proxy = ExposifyDebugger(receiver,printableClass, dataProcessor,  dataProvider)
    proxy.activeTemplate = usingTemplate
    return  proxy
}

