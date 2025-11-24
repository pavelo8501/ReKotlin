package po.test.misc.data.logging

import org.junit.jupiter.api.Test
import po.misc.context.component.Component
import po.misc.context.log_provider.LogProvider
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class TestLogProvider : Component {

    class EmmitableClass: LogProvider {
        override val logProcessor: LogProcessor<EmmitableClass, LogMessage> = createLogProcessor()
    }

    private val logger = createLogProcessor()

    @Test
    fun `EmmitableClass is capable to obtaining registered handler and remove it on completion`(){

        val emmitableClass = EmmitableClass()
        val initialMessage = infoMsg("Initial", "Some message")
        val proceduralFlow = logger.createProceduralFlow(initialMessage)
        val registration = emmitableClass.useLogHandler(proceduralFlow)
        assertNotNull(registration)
        val handler = emmitableClass.logProcessor.loader.loaderRoutine()
        assertSame(proceduralFlow, handler)
        assertNotNull(emmitableClass.logProcessor.logForwarder.handlerRegistrations.firstOrNull()){reg->
            assertSame(proceduralFlow, reg.handler)
        }
        proceduralFlow.complete()
        assertEquals(0,  emmitableClass.logProcessor.logForwarder.handlerRegistrations.size)
    }

    @Test
    fun `Top component's logger registers all its active handlers`(){

        val emmitableClass = EmmitableClass()
        val initialMessage = infoMsg("Initial", "Some message")
        val flow = logger.createProceduralFlow(initialMessage)

        val registration = emmitableClass.useLogHandler(logger)
        assertNotNull(registration)
        val handler = emmitableClass.logProcessor.loader.loaderRoutine()
        assertNotNull(handler)
        logger.finalizeHandler(flow)
        assertEquals(0,  emmitableClass.logProcessor.logForwarder.handlerRegistrations.size)
    }

    @Test
    fun `EmmitableClass message buss successfully overwritten by the top component`(){

        val initialMessage = infoMsg("Initial", "Some message")
        val flow = logger.createProceduralFlow(initialMessage)
        val emmitableClass = EmmitableClass()
        emmitableClass.useLogHandler(logger)
        val message =  emmitableClass.info("Info by EmmitableClass", "Info text")
        assertNotNull(flow.proceduralRecord.proceduralEntries.firstOrNull()){entry->
            assertNotNull(entry.logRecords.firstOrNull()){structuredLoggable->
                assertSame(message, structuredLoggable)
            }
        }
        flow.complete(output = true)
    }



}