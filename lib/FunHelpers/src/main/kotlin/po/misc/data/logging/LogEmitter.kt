package po.misc.data.logging

import po.misc.context.CTX
import po.misc.data.helpers.output
import po.misc.data.printable.Printable
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import po.misc.debugging.DebugTopic


interface LogEmitter {

    fun Any.notify(message: String, severity: SeverityLevel = SeverityLevel.INFO) {
        when(severity){
            SeverityLevel.INFO-> message.output(Colour.GREEN)
            SeverityLevel.WARNING-> message.output(Colour.Yellow)
            SeverityLevel.EXCEPTION-> message.output(Colour.RED)
            SeverityLevel.DEBUG-> message.output(Colour.BRIGHT_WHITE)
        }
    }
    fun  Any.log(data: PrintableBase<*>, severity: SeverityLevel = SeverityLevel.INFO) {
        data.echo()
    }
    fun <T: Printable> CTX.debug(message: String, template: PrintableTemplateBase<T>? = null, topic: DebugTopic = DebugTopic.General){
        message.output()
    }
}