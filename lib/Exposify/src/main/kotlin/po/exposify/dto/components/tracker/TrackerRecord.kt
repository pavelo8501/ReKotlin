package po.exposify.dto.components.tracker

import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ComponentType
import po.misc.time.ExecutionTimeStamp

data class TrackerRecord(
    override var id : Long,
    override val operation: CrudOperation = CrudOperation.Create,
    override val methodName: String = "",
    override val propertyName: String = "",
    override val oldValue : Any? = null,
    override val newValue: Any = "",
    val componentType: ComponentType = ComponentType.Tracker
) : ObservableData {

    override val componentName: String get()= componentType.componentName
    override val completeName: String get()= componentType.completeName

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

