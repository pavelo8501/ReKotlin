package po.misc.data.logging.processor.contracts

import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable


sealed interface TemplateActions{
    object LastRegistered: TemplateActions
}

interface TemplateHandler {

    val templateRecord: LoggableTemplate

    fun createTemplate(logRecord: StructuredLoggable):LoggableTemplate

    fun getEntry(action : TemplateActions = TemplateActions.LastRegistered):LoggableTemplate {
        return when (action) {
            is LastRegistered -> {
                templateRecord.getRecord(action)
            }
        }
    }

}

interface HandlerContract<T: StructuredLoggable>{
    var handler: TemplateHandler?
    var reasoning: Boolean
    fun processRecord(data: T) : T?
}