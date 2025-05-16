package po.exposify.dto.components.tracker

import po.exposify.dto.components.property_binder.interfaces.ObservableData
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.ModelDTO
import po.misc.time.ExecutionTimeStamp

data class TrackerRecord(
    override var id : Long,
    val crudOperation : CrudOperation = CrudOperation.Create,
    override val qualifiedName: String,
    override val type: ComponentType = ComponentType.Factory,
    override val oldValue : Any? = null,
    override val newValue: Any = ""
) : ObservableData {

    override val name: String = qualifiedName

    var executionTimestamp: ExecutionTimeStamp? = null
    var trackResult : Int = 0

    private val actionsList : MutableList<ObservableData> = mutableListOf()
    val actions : List<ObservableData> get() = actionsList.toList()

    fun finalize(dto: ModelDTO,  timestamp: ExecutionTimeStamp){
        id = dto.id
        executionTimestamp = timestamp
        trackResult = 1
    }

    fun addAction(action : ObservableData){
        actionsList.add(action)
    }
}

