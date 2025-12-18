package po.test.misc.debugging.stack_tracer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import po.misc.context.component.Component
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.models.LogMessage
import po.misc.debugging.classifier.HelperRecord
import po.misc.debugging.stack_tracer.TraceResolver
import po.misc.debugging.stack_tracer.reports.CallSiteReport
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestTraceResolver : Component {

    private var callSiteReport: CallSiteReport? = null
    private var resolver = TraceResolver(this, HelperRecord("TestTraceResolver", "debug")) {
        callSiteReport = it
    }

    private fun intermediaryMethod(){
        debug("Some subject", "Some text")
    }

    override fun notify(logMessage: LogMessage): LogMessage {
        resolver.processMsg(logMessage, false)
        return logMessage
    }

    @BeforeTest
    fun setup() {
        resolver.resolveTraceWhen(NotificationTopic.Debug)
        assertEquals(6, resolver.classifier.records.size)
        assertNotNull(resolver.classifier["TestTraceResolver"])
    }

    @BeforeEach
    fun teardown(){
        callSiteReport = null
    }

    @Test
    fun `TraceResolver creates call site report with no hop frame`() {
        val thisMethodName = "TraceResolver creates call site report with no hop frame"
        val debugMsg = debugMsg("Some subject", "Some text")
        resolver.processMsg(debugMsg, print = false)
        val report = assertNotNull(callSiteReport)
        assertEquals(0, report.hopFrames.size)
        assertEquals(thisMethodName, report.callerFrame.methodName)
        assertEquals(thisMethodName, report.registrationFrame.methodName)
    }

    @Test
    fun `TraceResolver creates call site report with 2 hop frames`() {
        val thisMethodName = "TraceResolver creates call site report with 2 hop frames"
        val notifyMethodName = "notify"
        intermediaryMethod()
        val report =  assertNotNull(callSiteReport)
        assertEquals(2, report.hopFrames.size)
        assertNotNull(report.hopFrames.firstOrNull{ it.methodName == "intermediaryMethod" })
        assertEquals(thisMethodName,  report.callerFrame.methodName)
        assertEquals(notifyMethodName,  report.registrationFrame.methodName)
    }
}