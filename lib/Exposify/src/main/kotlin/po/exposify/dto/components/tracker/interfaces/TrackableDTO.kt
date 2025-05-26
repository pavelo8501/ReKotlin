package po.exposify.dto.components.tracker.interfaces

import po.exposify.dto.components.property_binder.interfaces.ObservableData
import po.misc.interfaces.Identifiable
import po.misc.time.ExecutionTimeStamp

interface TrackableDTO :  Identifiable {
    val name: String
    val executionTimeStamp: ExecutionTimeStamp
    val records : List<ObservableData>
    val childTrackers : MutableList<TrackableDTO>

    override val qualifiedName: String
        get() = name
}