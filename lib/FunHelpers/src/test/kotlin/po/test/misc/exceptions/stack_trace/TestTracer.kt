package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.context.component.Component
import po.misc.data.output.output
import po.misc.debugging.models.InstanceInfo
import po.misc.debugging.stack_tracer.TraceOptions
import po.misc.exceptions.extractTrace
import po.misc.debugging.stack_tracer.ExceptionTrace
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestTracer: Component {

    private class Thrower {
        fun throwException(message: String = "Exception message") {
            throw Exception(message)
        }
    }

//    @Test
//    fun `Tracer picks correct frame`() {
//        val info: InstanceInfo = extractTrace(TraceOptions.InstanceInfo)
//        val thisMethodName = ::`Tracer picks correct frame`.name
//        val bestPick = assertNotNull(info.latestFrameMeta)
//        assertEquals(thisMethodName, bestPick.methodName)
//        info.output()
//        val trace: ExceptionTrace = trace()
//        assertEquals(bestPick.methodName, trace.bestPick.methodName)
//    }

    private fun throwingMethod(message: String = "Exception message") {
        throw Exception(message)
    }

    @Test
    fun `Tracer work as expected with exceptions`() {
        val thisMethodName = ::throwingMethod.name
        val exception = assertThrows<Exception> { throwingMethod() }
        val trace: ExceptionTrace = extractTrace(exception)
        assertEquals(thisMethodName, trace.bestPick.methodName)
        trace.output()
    }

    @Test
    fun `Tracer work as expected with exceptions thrown in other classes`() {
        val thrower = Thrower()
        val exception = assertThrows<Exception> { thrower.throwException() }
        val trace: ExceptionTrace = extractTrace(exception)
        val methodName = thrower::throwException.name
        val frameMeta =  trace.bestPick
        assertEquals(methodName, frameMeta.methodName)
        frameMeta.output()
    }

}