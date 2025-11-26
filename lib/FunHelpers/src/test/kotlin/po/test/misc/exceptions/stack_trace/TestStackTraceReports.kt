package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.stack_trace.CallSiteReport
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trace
import kotlin.test.assertTrue

class TestStackTraceReports {

    class SubClass(): TraceableContext{
        fun onMethod(){
            val trace : ExceptionTrace = trace(TraceCallSite(::onMethod))
            trace.printCallSite()
        }
        fun createTrace():  ExceptionTrace{
            return  trace(TraceCallSite(::createTrace))
        }
    }

    @Test
    fun `Call site report work as expected`(){
        val subClass = SubClass()
        val trace = subClass.createTrace()
        val report :  CallSiteReport =  ExceptionTrace.callSiteReport(trace)
        val render =  report.callSiteReport.render(report)
        render.output()
        assertTrue {
            render.contains("createTrace")
        }
    }

    @Test
    fun `Function call can be resolved to an actual call-site`() {
        val subClass = SubClass()
        subClass.onMethod()
    }

}