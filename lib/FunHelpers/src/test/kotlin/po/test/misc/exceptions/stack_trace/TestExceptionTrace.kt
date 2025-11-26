package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.test.misc.exceptions.setup.TraceNotifier
import po.test.misc.exceptions.setup.notifyOrNotExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestExceptionTrace: TraceableContext {

    private val notifier = TraceNotifier(notifyOnValue = 300)

    fun intermediaryMethod(value: Int): ExceptionTrace? {
        return notifier.notifyOrNot(value)
    }

    fun callbackMethod(value: Int, valueProvider: (Int) -> ExceptionTrace?): ExceptionTrace? {
        return valueProvider(value)
    }
    
    @Test
    fun `Call site stackTrace work as expected`() {
        val stackTrace = notifier.notifyOrNot(300)
        assertNotNull(stackTrace) { exTrace ->
            assertEquals(2, exTrace.stackFrames.size)
        }
        val registered = stackTrace.stackFrames.first()
        val initiated = stackTrace.stackFrames.last()
        assertEquals("notifyOrNot", registered.methodName)
        assertEquals("Call site stackTrace work as expected", initiated.methodName)
    }

    @Test
    fun `Call site stackTrace with multiple hops`() {

        val thisMethodName = ::`Call site stackTrace with multiple hops`.name
        val hopMethodName = ::intermediaryMethod.name

        val stackTrace = intermediaryMethod(300)
        assertNotNull(stackTrace) { exTrace ->
            assertEquals(3, exTrace.stackFrames.size)
        }
        val registered = stackTrace.stackFrames.first()
        val initiated = stackTrace.stackFrames.last()
        assertEquals("notifyOrNot", registered.methodName)
        assertEquals(thisMethodName, initiated.methodName)

        val hopFrame = stackTrace.stackFrames[1]
        assertEquals(hopMethodName, hopFrame.methodName)
    }

    @Test
    fun `Call site stackTrace with multiple hops and callback`() {

        val stackTrace = callbackMethod(300) {
            notifier.notifyOrNot(it)
        }

        val thisMethodName = ::`Call site stackTrace with multiple hops and callback`.name
        assertNotNull(stackTrace) { exTrace ->
            assertEquals(3, exTrace.stackFrames.size)
        }
        val registered = stackTrace.stackFrames.first()
        val initiated = stackTrace.stackFrames.last()
        assertEquals("notifyOrNot", registered.methodName)
        assertEquals(thisMethodName, initiated.methodName)

        val hopFrame = stackTrace.stackFrames[1]
        assertEquals("callbackMethod", hopFrame.methodName)
    }

    @Test
    fun `Call site stackTrace with multiple hops and extension`() {
        val thisMethodName = ::`Call site stackTrace with multiple hops and extension`.name
        val stackTrace = notifier.notifyOrNotExtension(300)
        assertNotNull(stackTrace) { exTrace ->
            assertEquals(3, exTrace.stackFrames.size)
        }
        val registered = stackTrace.stackFrames.first()
        val initiated = stackTrace.stackFrames.last()
        assertEquals("notifyOrNot", registered.methodName)
        assertEquals(thisMethodName, initiated.methodName)
        val hopFrame = stackTrace.stackFrames[1]
        assertEquals("notifyOrNotExtension", hopFrame.methodName)
    }

}