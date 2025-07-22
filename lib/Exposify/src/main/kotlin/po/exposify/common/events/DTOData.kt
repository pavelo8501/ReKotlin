package po.exposify.common.events

import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.context.Identifiable


data class DTOData(
    private val tracker: DTOTracker<*, *, *>,
    val message: String,

): PrintableBase<DTOData>(Debug){
    override val self: DTOData = this

   // override val itemId: ValueBased = DTOClass
    val currentOperation: CrudOperation get(){
      return  tracker.activeRecord.operation
    }

    companion object: PrintableCompanion<DTOData>({DTOData::class}){
        val Stats = createTemplate{
            next { "Active operation: $currentOperation" }
        }
        val Debug = createTemplate{
            next { message }
        }
    }
}
