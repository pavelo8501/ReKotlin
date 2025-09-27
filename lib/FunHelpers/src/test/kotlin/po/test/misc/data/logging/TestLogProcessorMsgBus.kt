package po.test.misc.data.logging

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.logging.LogEmitter
import po.misc.data.logging.processor.LogProcessor
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class TestLogProcessorMsgBus: LogEmitter  {

    val processor = LogProcessor(this)

    class CompositionSibling(
       private  val parentProcessor: LogProcessor
    ): LogEmitter{

        internal val siblingsProcessor = LogProcessor(this,  parentProcessor)

        override fun info(message: String, subject: String?) {
            siblingsProcessor.info(message, subject)
        }
    }

    @Test
    fun `Messages propagate as expected`(){
        val someObject = CompositionSibling(processor)
        someObject.info("Some info")
        assertEquals(1, someObject.siblingsProcessor.records.size)
        val fromSibling =  assertNotNull(processor.records.firstOrNull())
        val parentContext = assertNotNull(fromSibling.parentContext)
        assertSame(this, parentContext)
    }
}