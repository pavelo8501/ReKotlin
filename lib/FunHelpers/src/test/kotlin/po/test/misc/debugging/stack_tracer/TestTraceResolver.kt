package po.test.misc.debugging.stack_tracer

import po.misc.context.component.Component
import po.misc.data.logging.NotificationTopic
import po.misc.data.logging.StructuredLoggable
import po.misc.data.logging.models.LogMessage
import po.misc.debugging.classifier.HelperRecord
import po.misc.debugging.stack_tracer.TraceResolver
import po.misc.exceptions.stack_trace.CallSiteReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestTraceResolver : Component {

    private var callSiteReport: CallSiteReport? = null

    private var resolver = TraceResolver(this, HelperRecord("TestTraceResolver", "debug")) {
        callSiteReport = it
    }
    override fun notify(logMessage: LogMessage): StructuredLoggable {
        resolver.process(logMessage)
        return logMessage
    }
    private fun intermediaryMethod(){
        debug("Some subject", "Some text")
    }


    fun `TraceResolver creates call-site report correctly`() {
        resolver.resolveTraceWhen(NotificationTopic.Debug)
        intermediaryMethod()
        assertNotNull(callSiteReport){
            assertEquals(0, it.hopFrames.size)
        }
    }


    fun `Call site report with 1 hop frame`() {
        resolver = TraceResolver(this)
        var thisReport : CallSiteReport? = null
        resolver.resolveTraceWhen(NotificationTopic.Debug)
        resolver.traceResolved {
            thisReport = it
        }
        intermediaryMethod()
        assertNotNull(thisReport){report->
            assertEquals(1, report.hopFrames.size)
            assertEquals("intermediaryMethod", report.hopFrames.first().methodName)
        }
    }
}