package po.lognotify.notification.models


import po.lognotify.tasks.ExecutionStatus
import po.misc.data.printable.knowntypes.PropertyData

data class ActionData(
    val actionName: String,
    val actionStatus: ExecutionStatus,
    val propertySnapshot: List<PropertyData> = emptyList()
)