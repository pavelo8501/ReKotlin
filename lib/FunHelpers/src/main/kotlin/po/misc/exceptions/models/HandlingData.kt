package po.misc.exceptions.models

import po.misc.data.printable.PrintableBase
import po.misc.exceptions.ManagedException.ExceptionEvent
import po.misc.context.CTX


class ExceptionEventGroup<T: Any>(
    val event: ExceptionEvent
){
    val items: MutableList<T> = mutableListOf()
    fun addItem(item:T){
        items.add(item)
    }
}


data class ExceptionEventData(
    private val holder: ExceptionData,
    val contextName: String,
    val fullyQualifiedName: String,
    var message: String
){

    val event: ExceptionEvent  get() = holder.event
    val producer: CTX get() = holder.producer

    private var backTraceRecordsBacking: MutableList<PrintableBase<*>> = mutableListOf()
    val backTraceRecords: List<PrintableBase<*>> get () = backTraceRecordsBacking.toList()
    var stackTraceElement: StackTraceElement? = null

    fun addBacktrace(record:PrintableBase<*>){
        backTraceRecordsBacking.add(record)
    }

    fun addMessage(msg: String){
        message = msg
    }

    companion object{
        fun create(exceptionData: ExceptionData, context: CTX, message: String = ""): ExceptionEventData{
            return ExceptionEventData(
                exceptionData,
                message = message,
                contextName = context.identity.name,
                fullyQualifiedName = context::class.qualifiedName.toString()
            )
        }
    }
}

class ExceptionData2(
    val event: ExceptionEvent,
    val message: String,
    val producer: CTX?,
){

    var auxData: PrintableBase<*>? = null
        private set

    var thisStackTraceElement: StackTraceElement? = null
        private set

    var stackTraceList: List<StackTraceElement> = emptyList()
        private set

    fun addStackTrace(stackTrace: List<StackTraceElement>):ExceptionData2{
        if(producer != null){
            thisStackTraceElement =  producer.identity.parentIdentity?.let {parentIdentity->
                stackTrace.firstOrNull { it.className == parentIdentity.qualifiedName }
            }?:run {
                stackTrace.firstOrNull { it.className == producer?.identity?.qualifiedName }
            }
        }else{
            stackTraceList = stackTrace
        }
        return this
    }

    fun provideAuxData(data: PrintableBase<*>?):ExceptionData2{
        auxData = data
        return this
    }

}



data class ExceptionData(
    val event: ExceptionEvent,
    val producer: CTX
) {
    val events:ExceptionEventGroup<ExceptionEventData> =  ExceptionEventGroup(event)
    private var savedTrace: List<StackTraceElement> = listOf()

    fun addMessage(msg: String){
        events.items.lastOrNull()?.addMessage(msg)
    }

    fun addBacktrace(record:PrintableBase<*>, producer: CTX){

        events.items.forEach { println(it) }

        events.items.forEach {
            if(it.producer == producer){
                it.addBacktrace(record)
            }
        }
        val foundEvent = events.items.firstOrNull { it.producer ===  producer }
        foundEvent?.addBacktrace(record)
    }

    fun addEvent(eventData: ExceptionEventData){
        events.addItem(eventData)
    }

    fun addStackTraceElement(trace: List<StackTraceElement>){
        savedTrace = trace
        trace.forEach { element->
           val eventRecord =  events.items.map { it }.firstOrNull {event-> event.fullyQualifiedName == element.className }
           if(eventRecord != null){
               eventRecord.stackTraceElement  = element
           }
        }
    }

    override fun toString(): String {
       return ""
//       return when(event){
//            ExceptionEvent.Thrown->{
//                "First registered in $contextName Reason:$message"
//            }
//            ExceptionEvent.Executed->{
//                "Executed in $contextName Reason:$message"
//            }
//           ExceptionEvent.HandlerChanged -> {
//               "Handler changed in $contextName Reason:$message"
//           }
//           ExceptionEvent.Rethrown -> {
//               "Rethrown in $contextName Reason:$message"
//           }
//       }
    }

    companion object{

        fun createThrown(producer: CTX):ExceptionData{
            return ExceptionData(
                ExceptionEvent.Thrown,
                producer
            ).apply {
                events.addItem(
                    ExceptionEventData.create(this, producer)
                )
            }
        }


        fun fromExceptionEvent(event: ExceptionEvent, producer: CTX): ExceptionData{
          return  ExceptionData(event, producer).apply {
                events.addItem(
                    ExceptionEventData.create(this, producer)
                )
            }
        }

        fun createRethrownEvent(producer: CTX, message: String = ""):ExceptionData{
           return ExceptionData(ExceptionEvent.Rethrown, producer).apply {
               events.addItem(
                   ExceptionEventData.create(this, producer, message)
               )
           }
        }

        fun createExecutedEvent(producer: CTX, message: String = ""):ExceptionData{
            return ExceptionData(ExceptionEvent.Executed, producer).apply {
                events.addItem(
                    ExceptionEventData.create(this, producer, message)
                )
            }
        }
    }
}