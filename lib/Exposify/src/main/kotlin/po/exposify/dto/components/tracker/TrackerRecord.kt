package po.exposify.dto.components.tracker

import po.exposify.dto.components.bindings.property_binder.interfaces.ObservableData
import po.exposify.dto.interfaces.ModelDTO
import po.misc.time.ExecutionTimeStamp

data class TrackerRecord(
    val component: DTOTracker<*,*>,
    override var id : Long,
    override val operation: CrudOperation = CrudOperation.Create,
    override val methodName: String = "",
    override val propertyName: String = "",
    override val oldValue : Any? = null,
    override val newValue: Any = "",
) : ObservableData {

    override val contextName: String get()= component.contextName
    override var sourceName: String = component.sourceName

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

