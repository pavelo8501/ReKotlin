package po.lognotify.debug

import po.lognotify.TasksManaged
import po.lognotify.classes.notification.LoggerDataProcessor
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableContext


data class DebugParams<D: PrintableBase<D>>(
    val message: String,
    val template:PrintableTemplate<D>?
)

class DebugProxy<T: IdentifiableContext, P: PrintableBase<P>>(
    val receiver:T,
    val printableClass: PrintableCompanion<P>,
    private val dataProcessor: LoggerDataProcessor?,
    val dataProvider: (DebugParams<P>)-> P
){
    fun debug(message: String){
        val printable =  dataProvider.invoke(DebugParams(message, null))
        dataProcessor?.debug(printable, printableClass, null)
    }

    fun debug(message: String, template: PrintableTemplate<P>){
        val printable =  dataProvider.invoke(DebugParams(message, template))
        dataProcessor?.debug(printable, printableClass, template)
    }
}

fun <T: IdentifiableContext, P: PrintableBase<P>> TasksManaged.debugProxy(
    receiver:T,
    printableClass: PrintableCompanion<P>,
    dataProvider: (DebugParams<P>)-> P
):DebugProxy<T, P>{
    val dataProcessor = this.logHandler
    return  DebugProxy(receiver,printableClass, dataProcessor,  dataProvider)
}