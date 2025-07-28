package po.exposify.common.events

import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion



data class ContextData(
    val message: String,
): PrintableBase<ContextData>(Debug){
    override val self: ContextData = this
  //  override val producer: IdentifiableClass


    companion object: PrintableCompanion<ContextData>({ContextData::class}){

        val Debug = createTemplate(){
            next {
                ""
            }
        }
    }

}

