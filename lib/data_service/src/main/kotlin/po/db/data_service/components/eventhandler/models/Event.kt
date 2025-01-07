package po.db.data_service.components.eventhandler.models

import po.db.data_service.components.eventhandler.enums.EventType


data class Event(
    val module : String,
    val msg: String,
    val type: EventType,
    val timestamp: Long
)