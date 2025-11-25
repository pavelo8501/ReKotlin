package po.test.misc.exceptions.stack_trace

import org.junit.jupiter.api.Test
import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.TraceCallSite
import po.misc.exceptions.stack_trace.ExceptionTrace
import po.misc.exceptions.trace

class TestFunCallSiteResolver {

    class SubClass(): TraceableContext{
        fun onMethod(){
            val trace : ExceptionTrace = trace(TraceCallSite(::onMethod))

            trace.printCallSite()
        }
    }

    @Test
    fun `Function call can be resolved to an actual call-site`() {
        val subClass = SubClass()
        subClass.onMethod()
    }

}