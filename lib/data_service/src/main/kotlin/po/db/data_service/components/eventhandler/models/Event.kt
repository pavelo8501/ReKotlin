package po.db.data_service.components.eventhandler.models

import po.db.data_service.components.eventhandler.enums.EventType


data class Event(
    val module : String,
    val msg: String,
    val type: EventType,
    val timestamp: Long
){

    var elapsedMills : Long? = null
        private set

    val elapsedTime: String
        get(){
            elapsedMills?.let {
                val timeInSeconds : Float  = (it / 100f)
                return "Elapsed time $timeInSeconds sec."
            }
            return "Elapsed time - N/A"
        }

    val subEvents = mutableListOf<Event>()

    fun setElapsed(elapsed: Long?){
        elapsedMills = elapsed
    }
}