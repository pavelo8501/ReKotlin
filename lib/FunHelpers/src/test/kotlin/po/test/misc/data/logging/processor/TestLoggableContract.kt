package po.test.misc.data.logging.processor

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.data.logging.LoggableTemplate
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.procedural.ProceduralFlow
import po.misc.data.logging.procedural.ProceduralRecord
import po.misc.data.logging.processor.contracts.ContextStartsNewNode
import po.misc.data.logging.processor.contracts.HandlerContract
import po.misc.data.logging.processor.contracts.TemplateHandler
import po.misc.data.logging.processor.createLogProcessor
import po.test.misc.data.logging.LoggerTestBase
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame


class TestLoggableContract : LoggerTestBase(), Component {


    private class LogTemplater(
        val procedural: ProceduralRecord,
        val contract: HandlerContract<StructuredLoggable>
    ) : TemplateHandler {


        override val templateRecord: LoggableTemplate get() = procedural

        init { contract.handler = this }

        override fun createTemplate(logRecord: StructuredLoggable): LoggableTemplate {
            return ProceduralFlow.toProceduralRecord(logRecord)
        }

        fun processRecord(logRecord: StructuredLoggable, reasoning: Boolean = false): StructuredLoggable? {
            contract.reasoning = reasoning
            return contract.processRecord(logRecord)
        }
    }

    private val component1 = ProceduralTestComponent()

    @Test
    @Order(1)
    fun `NewContextNewProcedural work as expected`() {

        val initialMessage = infoMsg("Subject", "Initial message")
        val procedural =  ProceduralFlow.toProceduralRecord(initialMessage)

        assertSame(procedural.logRecord, initialMessage)
        val contract = ContextStartsNewNode<StructuredLoggable>()
        val templater = LogTemplater(procedural, contract)
        val message1 = generateMessage(1)
        val message2 = generateMessage(2)
        templater.processRecord(message1)
        templater.processRecord(message2)
        assertEquals(2, initialMessage.getRecords().size)

        val foreignInitialMessage = component1.generateMessage(value =  5)
        templater.processRecord(foreignInitialMessage)

        val newProceduralMessage = assertNotNull(procedural.proceduralEntries.lastOrNull()?.proceduralRecords?.lastOrNull())
        assertEquals(foreignInitialMessage.text, newProceduralMessage.text)
        assertSame(foreignInitialMessage, newProceduralMessage.logRecord)

        val message1Component1 = component1.generateMessage(value =  10)
        templater.processRecord(message1Component1)
        assertEquals(1, newProceduralMessage.proceduralEntries.size)
        assertNotNull(newProceduralMessage.proceduralEntries.first().logRecords.firstOrNull()){logMessage->
            assertSame(message1Component1, logMessage)
        }
        val message2Component1 = component1.generateMessage(value =  20)
        templater.processRecord(message2Component1)
        assertEquals(2, newProceduralMessage.logRecords.size)
        assertSame(message2Component1, newProceduralMessage.logRecords[1])
        procedural.outputRecord()
    }

    @Test
    @Order(2)
    fun `NewContextNewProcedural integration to flow work same way as in previous test`() {
        val processor = createLogProcessor()
        val initialMessage = infoMsg("Subject", "Initial message")
        val flow =  processor.createProceduralFlow(initialMessage)
        val procedural = flow.proceduralRecord
        assertSame(procedural.logRecord, initialMessage)
        val message1 = generateMessage(1)
        val message2 = generateMessage(2)
        flow.processRecord(message1)
        flow.processRecord(message2)
        assertEquals(2, initialMessage.getRecords().size)

        val foreignInitialMessage = component1.generateMessage(value =  5)
        flow.processRecord(foreignInitialMessage)

        val newProceduralMessage = assertNotNull(procedural.proceduralEntries.lastOrNull()?.proceduralRecords?.lastOrNull())
        assertEquals(foreignInitialMessage.text, newProceduralMessage.text)
        assertSame(foreignInitialMessage, newProceduralMessage.logRecord)

        val message1Component1 = component1.generateMessage(value =  10)
        flow.processRecord(message1Component1)
        assertEquals(1, newProceduralMessage.proceduralEntries.size)
        assertNotNull(newProceduralMessage.proceduralEntries.first().logRecords.firstOrNull()){logMessage->
            assertSame(message1Component1, logMessage)
        }
        val message2Component1 = component1.generateMessage(value =  20)
        flow.processRecord(message2Component1)
        assertEquals(2, newProceduralMessage.logRecords.size)
        assertSame(message2Component1, newProceduralMessage.logRecords[1])
        
        flow.complete(output = true)

    }
}