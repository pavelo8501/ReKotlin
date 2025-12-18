package po.test.misc.debugging.stack_tracer.reports

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.debugging.classifier.PackageClassifier
import po.misc.debugging.stack_tracer.ExceptionTrace
import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.stack_tracer.reports.CallSiteReport
import po.misc.exceptions.Tracer
import kotlin.test.assertEquals

class TestCallSiteReport {

   private val trace  = Tracer().trace

    @Test
    fun `Call site report`(){

        val metas = trace.frameMetas.filter { it.isUserCode && it.packageRole != PackageClassifier.PackageRole.Helper }.take(5)
        trace.frameMetas = metas
        val report = trace.callSite()
        assertEquals(3, report.hopFrames.size)
        val render = CallSiteReport.callSiteTemplate.render(report)
        render.output()
    }

}