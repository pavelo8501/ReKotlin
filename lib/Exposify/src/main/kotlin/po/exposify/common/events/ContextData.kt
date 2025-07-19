package po.exposify.common.events

import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.Identifiable
import po.misc.interfaces.asIdentifiable


data class ContextData(
    val message: String,
): PrintableBase<ContextData>(Debug){
    override val self: ContextData = this

  //  override val producer: IdentifiableClass

    override val producer: Identifiable = asIdentifiable("sss", "sssss")


    companion object: PrintableCompanion<ContextData>({ContextData::class}){

        fun prefix(ctx: Identifiable): String{
           return "Debug in  ${ctx.contextName}"
        }

        val Debug = PrintableTemplate<ContextData>("Debug"){
            ""
        }

    }

}

