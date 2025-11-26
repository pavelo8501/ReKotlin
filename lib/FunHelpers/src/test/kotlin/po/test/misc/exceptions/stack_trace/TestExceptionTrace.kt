package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.data.output.output
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trace

class TestExceptionTrace: TraceableContext {

    fun createTrace():  ExceptionTrace{
       return  trace(TraceCallSite(::createTrace))
    }

    @Test
    fun `Call site report work as expected`(){
        val trace = createTrace()
        val report =  ExceptionTrace.callSiteReport(trace)
        report.output()

    }

}