package po.exposify.common.events

import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.DTOTracker
import po.misc.data.console.PrintableTemplate
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.ValueBased

data class DTOEvent(
    private val tracker: DTOTracker<*,*>,
    val message: String,

): PrintableBase<DTOEvent>(Debug){
    override val self: DTOEvent = this

   // override val itemId: ValueBased = DTOClass
    override val emitter:  IdentifiableContext = tracker
    val currentOperation: CrudOperation get(){
      return  tracker.activeRecord.operation
    }

    companion object: PrintableCompanion<DTOEvent>({DTOEvent::class}){
        fun prefix(data:DTOEvent): String{
            return data.tracker.completeName
        }

        val Stats : PrintableTemplate<DTOEvent> = PrintableTemplate("Stats"){
            "${prefix(this)}: Active operation: $currentOperation"
        }

        val Debug : PrintableTemplate<DTOEvent> = PrintableTemplate("Debug"){
            "${prefix(this)}: $message"
        }
    }
}
