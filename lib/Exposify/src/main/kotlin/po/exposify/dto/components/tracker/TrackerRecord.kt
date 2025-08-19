package po.exposify.dto.components.tracker


data class TrackerRecord(
    val component: DTOTracker<*, *, *>,
    val operation: CrudOperation = CrudOperation.Create,
    val methodName: String = "",
){

    var trackResult : Int = 0

    val startTime: Long get() = component.executionTimeStamp.startTime
    val endTime: Long get() = component.executionTimeStamp.endTime
    val elapsed: Float get() = component.executionTimeStamp.elapsed

   // val updates: MutableList<PropertyUpdate<*>> = mutableListOf()


//    fun setPropertyUpdate(update: List<PropertyUpdate<*>>){
//        updates.addAll(updates)
//    }

    fun finalizeRecord(){
        trackResult = 1
    }
}

