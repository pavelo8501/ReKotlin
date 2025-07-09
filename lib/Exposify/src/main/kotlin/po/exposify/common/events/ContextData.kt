package po.exposify.common.events

import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext


data class ContextData(
    val ctx: IdentifiableClass,
    val message: String,
): PrintableBase<ContextData>(Debug), IdentifiableClass by  ctx{
    override val self: ContextData = this


    override val emitter: IdentifiableContext = this


    companion object: PrintableCompanion<ContextData>({ContextData::class}){

        fun prefix(ctx: IdentifiableContext): String{
           return "Debug in  ${ctx.contextName}"
        }

        val Debug : PrintableTemplate<ContextData> = PrintableTemplate("Debug"){
            "${prefix(this)}: $message"
        }

    }

}

