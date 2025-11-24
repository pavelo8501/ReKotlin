package po.misc.data.logging.procedural

import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.processor.contracts.ContextStartsNewNode
import po.misc.data.logging.processor.contracts.HandlerContract
import po.misc.data.logging.processor.contracts.TemplateHandler



class ProceduralTemplater(
    private val proceduralFlow: ProceduralFlow<*>,
    initialContract: HandlerContract<StructuredLoggable>? = null
) : TemplateHandler {

    val contract: HandlerContract<StructuredLoggable> = initialContract?: ContextStartsNewNode<StructuredLoggable>()
    override val templateRecord: LoggableTemplate get() = proceduralFlow.proceduralRecord

    init { contract.handler = this }

    fun processRecord(logRecord: StructuredLoggable, reasoning: Boolean = false): StructuredLoggable? {
        contract.reasoning = reasoning
        return contract.processRecord(logRecord)
    }

    override fun createTemplate(logRecord: StructuredLoggable): LoggableTemplate {
        return ProceduralFlow.toProceduralRecord(logRecord)
    }
}