package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.stack_trace.CallSiteReport
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trace
import po.test.misc.exceptions.setup.TraceNotifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestStackTraceReports {

    class SubClass() : TraceableContext {
        fun createTrace(): ExceptionTrace {
            return trace(TraceCallSite("TestStackTraceReports", ::createTrace))
        }
    }
    private val notifier = TraceNotifier(notifyOnValue = 300)

    fun intermediaryMethod(value: Int): ExceptionTrace? {
        return notifier.notifyOrNot(value)
    }

    @Test
    fun `Call site report work as expected`() {
        val thisFunName = ::`Call site report work as expected`.name
        val registeredFunName = SubClass::createTrace.name
        val subClass = SubClass()
        val trace = subClass.createTrace()
        val report: CallSiteReport = ExceptionTrace.callSiteReport(trace)
        val render = report.formattedString

        render.output()
        assertTrue { render.contains(registeredFunName) && render.contains(thisFunName) }
    }

    @Test
    fun `Call site report render hops as expected`() {
        val stackTrace = intermediaryMethod(300)
        assertNotNull(stackTrace)
        val report = ExceptionTrace.callSiteReport(stackTrace)
        val reportRender = report.formattedString
        assertEquals(1, report.hopFrames.size)
        reportRender.output()
    }

}

