package po.exposify.dto.components.tracker.interfaces


import po.misc.time.ExecutionTimeStamp

interface TrackableDTO{
    val executionTimeStamp: ExecutionTimeStamp
   // val records : List<ObservableData>
    val childTrackers : MutableList<TrackableDTO>
}