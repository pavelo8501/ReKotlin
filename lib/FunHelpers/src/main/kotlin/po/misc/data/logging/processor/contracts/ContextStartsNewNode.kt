package po.misc.data.logging.processor.contracts

import po.misc.context.tracable.TraceableContext
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.NotificationTopic
import po.misc.data.output.output
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.styles.Colour
import po.misc.debugging.ClassResolver
import po.misc.exceptions.managedException
import java.lang.IllegalStateException

// Each new context message create new procedural record
// Messages are stacked inline
// Procedural -> List<LogRecords>

class ContextStartsNewNode<T: StructuredLoggable>(
    override var handler: TemplateHandler? = null
): HandlerContract <T>, TraceableContext {

    override var reasoning: Boolean = false

    private val handlerUnsafe :TemplateHandler get() {
       return  handler.getOrThrow {
            throw IllegalStateException("TemplateHandler is unavailable")
        }
    }

    val processLastReceived : (T, LoggableTemplate) -> Unit = { received, lastTemplate ->

        if(received.context !== lastTemplate.context){
            val newProcedural = ProceduralFlow.toProceduralRecord(received)
            if(reasoning){
                "Created new Procedural record since logRecord received was from ${getContextName(received.context)}".output()
                "And last template context was ${getContextName(lastTemplate.context)}".output()
            }
            lastTemplate.logRecord.addRecord(newProcedural.logRecord)
            lastTemplate.addRecord(newProcedural)
        } else {
            if(reasoning){
                "Adding new $received to template $lastTemplate".output()
            }
            lastTemplate.addMessage(received)
        }
    }

    val newContextStartsNode : (StructuredLoggable, LoggableTemplate) -> Boolean = { received, lastTemplate ->

        if(received.context !== lastTemplate.context){
            false
        }else {
            val newNode = handlerUnsafe.createTemplate(received)
           // val newProcedural = ProceduralFlow.toProceduralRecord(received)
            if(reasoning){
                "Created new Procedural record since logRecord received was from ${getContextName(received.context)}".output()
                "And last template context was ${getContextName(lastTemplate.context)}".output()
            }
           // lastTemplate.logRecord.addRecord(newNode.logRecord)
            lastTemplate.addRecord(newNode)
            true
        }
    }

    private fun getContextName(context: TraceableContext): String{
        return ClassResolver.instanceName(context)
    }

    override fun processRecord(data: T): T? {
        val availableHandler = handler
        if(availableHandler != null){
            val lastReceived =   availableHandler.getEntry()
            processLastReceived(data, lastReceived)
            return data
        }else{
           val warning = notification("processRecord", "Unable to complete processRecord handler not defined", NotificationTopic.Warning)
           warning.output()
           return null
        }
    }
}