package po.test.misc.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.context.asIdentity
import po.misc.data.logging.ContextAware
import po.misc.data.logging.logEmitter
import po.misc.exceptions.ManagedException
import po.misc.context.tracable.TraceableContext
import po.misc.coroutines.CoroutineInfo
import po.misc.exceptions.trackable.TrackableException
import po.misc.exceptions.metaFrameTrace
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.StackFrameMeta
import po.misc.exceptions.raiseException
import po.misc.exceptions.raiseManagedException
import po.misc.exceptions.registerExceptionBuilder
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestContextExceptions: ContextAware {

    override val identity = asIdentity()
    override val emitter = logEmitter()

    val traces = mutableListOf<StackFrameMeta>()
    var trace: ExceptionTrace? = null

    class SomeException(val context: TraceableContext): Throwable("SomeException"), TrackableException {
        override val contextClass: KClass<*> = context::class
        override val exceptionTrace: ExceptionTrace = metaFrameTrace(contextClass)
        override val self = this
        override var coroutineInfo: CoroutineInfo? = null
    }

    @Test
    fun `Exception trace information is precise`() {
        assertThrows<ManagedException> {
            raiseManagedException("TestMessage") {
                trace = it
            }
        }
        val receivedTrace = assertNotNull(trace)
        assertEquals("Exception trace information is precise", receivedTrace.stackFrames.first().methodName)
    }

    @Test
    fun `Custom exceptions produce same result`(){
        registerExceptionBuilder {
            SomeException(this)
        }
        val exception = assertThrows<SomeException> {
            raiseException<SomeException>("Something")
        }
        val frames = exception.exceptionTrace.stackFrames
        assertEquals("Custom exceptions produce same result.[lambda]", frames.first().normalizedMethodName)
    }
}