package po.lognotify.eventhandler.models

import po.lognotify.eventhandler.exceptions.ProcessableException
import po.lognotify.logging.LoggingService
import po.lognotify.shared.enums.SeverityLevel

data class Event(
    val module : String,
    var msg: String = "",
    val type: SeverityLevel = SeverityLevel.INFO,
){

   var exception: ProcessableException? = null
       set(value){
           msg = value?.message.toString()
       }

   var startTime: Long =  System.nanoTime()
   private set

   var stopTime : Long? = null
   private set

   var elapsed: Float = 0.0F
       private set

   val elapsedTime: String
        get(){ stopTime?.let {
                return "Elapsed time $elapsed ms."
            }
            return "Elapsed time - N/A"
        }

    val subEvents = mutableListOf<Event>()

    fun setElapsed(start: Long, end: Long? = 0){
        startTime =start
        stopTime = end
    }

    fun setException(ex: ProcessableException): Event {
        exception = ex
        return this
    }
    /**
     * Public method for setting end time of the process execution
     *
     */
    fun stopTimer(){
        System.nanoTime().let {
            stopTime = it
            elapsed  = ((it - startTime) / 1000f)
        }
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