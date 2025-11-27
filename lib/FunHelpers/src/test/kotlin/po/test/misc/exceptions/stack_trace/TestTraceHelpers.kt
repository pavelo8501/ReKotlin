package po.test.misc.exceptions.stack_trace

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.ToString
import po.misc.data.output.output
import po.misc.exceptions.ExceptionPayload
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.Tracer
import po.misc.exceptions.stack_trace.CallSiteReport
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.stack_trace.extractTrace
import po.misc.types.k_class.simpleOrAnon
import kotlin.reflect.cast
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class TestTraceHelpers : TraceableContext {

    private  val thiClassName = this::class.simpleOrAnon
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


    fun createExceptionTrace(useMethod: String? = null): ExceptionTrace{
        if(useMethod != null){
          return  Tracer().extractTrace(TraceCallSite(thiClassName, methodName = useMethod))
        }
        return Tracer().extractTrace(TraceCallSite(thiClassName, methodName = null))
    }

    @Test
    fun `Trace option modify behaviour as expected`() {
        val trace = createExceptionTrace()
        assertNotNull(trace.frameMetas.firstOrNull { it.methodName == "createExceptionTrace" })
        val byMethodName = createExceptionTrace("Trace option modify behaviour as expected")
        assertNotNull(byMethodName.frameMetas.firstOrNull { it.methodName == "Trace option modify behaviour as expected" })
        assertNull(byMethodName.frameMetas.firstOrNull { it.methodName == "createExceptionTrace" } )
    }


    @Test
    fun `Throwable can be analyzed with acceptable level of precision(no guidelines provided)`(){
        val trace =  assertDoesNotThrow {
            val exception = Exception("Some string")
            exception.extractTrace()
        }
        assertEquals(thiClassName, trace.bestPick.simpleClassName)
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
        assertEquals(thiClassName, traceData.bestPick.simpleClassName)
        assertEquals("Throwable can be analyzed when  helpers method name provided", traceData.bestPick.methodName)
    }

    @Test
    fun `Throwable can be analyzed when cass name provided (class implementing TrackableContext)`(){
        val testHelpersSubClass = TestHelpersSubClass()
        val traceData =   assertDoesNotThrow {
            testHelpersSubClass.traceForParentContext(this)
        }
        assertEquals(
            thiClassName,
            traceData.bestPick.simpleClassName
        )
        assertEquals(
            "Throwable can be analyzed when cass name provided (class implementing TrackableContext)",
            traceData.bestPick.methodName
        )
    }

    @Test
    fun `extractTrace handles coroutines`() {
        suspend fun boom() { throw RuntimeException("Boom") }
        val exception = assertThrows<RuntimeException> {
            runBlocking {
                boom()
            }
        }
        val trace = exception.extractTrace()
        trace.output()
        assertEquals("boom", trace.bestPick.methodName)
    }

    @Test
    fun `extractTrace handles lambda`() {
        val f = { -> throw RuntimeException("Lambda!") }
        val ex = assertThrows<RuntimeException> { f() }
        val trace = ex.extractTrace()
        assertTrue {
            trace.bestPick.methodName.contains("f") &&
                    trace.bestPick.methodName.contains("extractTrace handles lambda")
        }
        trace.bestPick.output()
    }

    @Test
    fun `extractTrace handles thread entry`() {
        var ex: Throwable? = null
        val t = Thread {
            try {
                throw RuntimeException("Thread crash")
            } catch (e: Throwable) {
                ex = e
            }
        }
        t.start(); t.join()
        val trace = ex!!.extractTrace()
        assertNotEquals(trace.bestPick.methodName, "run")
        trace.bestPick.output()
    }
}