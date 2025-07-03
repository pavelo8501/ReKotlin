package po.exposify.common.events

import po.exposify.common.events.DTOEvent.Companion.prefix
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext


data class ContextEvent(
    val ctx: IdentifiableClass,
    val message: String,
): PrintableBase<ContextEvent>(Debug), IdentifiableClass by  ctx{
    override val self: ContextEvent = this


    override val emitter: IdentifiableContext = this


    companion object: PrintableCompanion<ContextEvent>({ContextEvent::class}){
        fun prefix(ctx: IdentifiableContext): String{
           return "Debug in  ${ctx.contextName}"
        }

        val Debug : PrintableTemplate<ContextEvent> = PrintableTemplate("Debug"){
            "${prefix(this)}: $message"
        }

    }

}

