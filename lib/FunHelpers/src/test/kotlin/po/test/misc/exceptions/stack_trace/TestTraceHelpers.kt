package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.context.TraceableContext
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.helpers.simpleOrAnon
import kotlin.reflect.cast
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TestTraceHelpers : TraceableContext {

    val thiCalssName = this::class.simpleOrAnon

    internal class TestHelpersSubClass(){
        var nullable: Int? = null

        fun castNotThrowing(nullableIntValue: Int? = null): ExceptionTrace? {
            nullable = nullableIntValue
            try {
                String::class.cast(nullable)
                return null
            } catch (th: ClassCastException) {
                return th.extractTrace(ExceptionPayload(th.message ?: "", "castNotThrowing", helperMethodName = true, this))
            }
        }
        fun traceForParentContext(testClass: TestTraceHelpers): ExceptionTrace{
            val exception = Exception("Some string")
            return exception.extractTrace(ExceptionPayload("Some string", "na", helperMethodName = false,  testClass))
        }

    }

    @Test
    fun `Throwable can be analyzed with acceptable level of precision(no guidelines provided)`(){
        val trace =  assertDoesNotThrow {
            val exception = Exception("Some string")
            exception.extractTrace()
        }
        assertEquals(thiCalssName, trace.bestPick.simpleClassName)
        assertEquals(
            "Throwable can be analyzed with acceptable level of precision(no guidelines provided)",
            trace.bestPick.methodName
        )
    }

    @Test
    fun `Throwable can be analyzed when  helpers method name provided`(){
        val testHelpersSubClass = TestHelpersSubClass()
        val trace =  assertDoesNotThrow {
            testHelpersSubClass.castNotThrowing(10)
        }
        val traceData = assertNotNull(trace)
        assertEquals(
            thiCalssName,
            traceData.bestPick.simpleClassName
        )
        assertEquals(
            "Throwable can be analyzed when  helpers method name provided",
            traceData.bestPick.methodName
        )
    }

    @Test
    fun `Throwable can be analyzed when cass name provided (class implementing TrackableContext)`(){

        val testHelpersSubClass = TestHelpersSubClass()
        val traceData =   assertDoesNotThrow {
            testHelpersSubClass.traceForParentContext(this)
        }
        assertEquals(
            thiCalssName,
            traceData.bestPick.simpleClassName
        )
        assertEquals(
            "Throwable can be analyzed when cass name provided (class implementing TrackableContext)",
            traceData.bestPick.methodName
        )
    }
}