package po.exposify.components.eventhandler.models

import po.db.data_service.components.eventhandler.enums.EventType
import po.db.data_service.components.logger.LoggingService

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
                val timeInSeconds : Float  = (it / 1000f)
                return "Elapsed time $timeInSeconds ms."
            }
            return "Elapsed time - N/A"
        }

    val subEvents = mutableListOf<Event>()

    /**
     * Public method for setting end time of the process execution
     * @param elapsed Long timestamp
     */
    fun setElapsed(elapsed: Long?){
        elapsedMills = elapsed
    }

    /**
     * Prints the details of the event and its sub-events recursively with proper indentation.
     *
     * The method formats the output to clearly indicate the nested nature of events, making it
     * easier to analyze hierarchical structures. It can either print to the console or log
     * the output using a provided `LoggingService`.
     *
     * @param indent The current indentation level, used internally for recursive calls to format the output.
     *               Defaults to `0` for the root event.
     * @param logService An optional instance of `LoggingService` for logging the event details.
     *                   If provided, the method logs the details using the `info` method of the service.
     *                   If not provided, the details are printed to the console using `println`.
     *
     * Example usage:
     * ```
     * val rootEvent = Event("RootModule", "Root event triggered", EventType.INFO, System.currentTimeMillis())
     * val subEvent = Event("SubModule", "Sub-event occurred", EventType.ACTION, System.currentTimeMillis())
     * rootEvent.subEvents.add(subEvent)
     *
     * // Print to console
     * rootEvent.print()
     *
     * // Log using LoggingService
     * val logService = LoggingService()
     * rootEvent.print(logService = logService)
     * ```
     *
     * Example output (console or logger):
     * ```
     * - [INFO] RootModule: Root event triggered (Elapsed time: N/A)
     *   - [ACTION] SubModule: Sub-event occurred (Elapsed time: N/A)
     * ```
     */
    fun print(indent: Int = 0, logService: LoggingService? = null) {
        val indentString = "  ".repeat(indent)
        val eventDetails = "$indentString- [${type.name}] $module: $msg (${elapsedTime})"
        logService?.info(eventDetails) ?: println(eventDetails)
        subEvents.forEach { it.print(indent + 1, logService) }
    }

}