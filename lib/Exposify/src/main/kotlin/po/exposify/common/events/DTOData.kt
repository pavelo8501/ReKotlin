package po.exposify.common.events

import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.context.Identifiable
import po.misc.data.printable.companion.nextLine
import po.misc.types.token.TypeToken


data class DTOData(
    private val tracker: DTOTracker<*, *, *>,
    val message: String,

): PrintableBase<DTOData>(this){
    override val self: DTOData = this

    val currentOperation: CrudOperation get(){
      return  tracker.activeRecord.operation
    }

    companion object: PrintableCompanion<DTOData>(TypeToken.create()){

        val Stats = createTemplate{
            nextLine { "Active operation: $currentOperation" }
        }

        val Debug = createTemplate{
            nextLine{ message }
        }
    }
}
