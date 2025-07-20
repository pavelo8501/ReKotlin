package po.exposify.common.events

import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.misc.data.printable.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.Identifiable
import po.misc.interfaces.asIdentifiable

data class DTOData(
    private val tracker: DTOTracker<*, *, *>,
    val message: String,

): PrintableBase<DTOData>(Debug){
    override val self: DTOData = this

   // override val itemId: ValueBased = DTOClass
    override val producer:  Identifiable = asIdentifiable(tracker.contextName, "DTOData")
    val currentOperation: CrudOperation get(){
      return  tracker.activeRecord.operation
    }

    companion object: PrintableCompanion<DTOData>({DTOData::class}){

        val Stats : PrintableTemplate<DTOData> = PrintableTemplate("Stats"){
            "Active operation: $currentOperation"
        }

        val Debug : PrintableTemplate<DTOData> = PrintableTemplate("Debug"){
            message
        }
    }
}
