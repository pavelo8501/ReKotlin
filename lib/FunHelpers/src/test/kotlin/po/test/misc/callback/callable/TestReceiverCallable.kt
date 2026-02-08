package po.test.misc.callback.callable

import org.junit.jupiter.api.assertThrows
import po.misc.callbacks.callable.FunctionCallable
import po.misc.data.strings.stringify
import po.misc.debugging.stack_tracer.reports.CallSiteReport
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestReceiverCallable {

    fun throwingMethod(parameter: TestReceiverCallable): String{
        throw Throwable("Some throwable")
    }
    @Test
    fun `Callable can identify source of exception`(){
        val callable = FunctionCallable<TestReceiverCallable, String>(::throwingMethod)
        var report: CallSiteReport? = null
        assertThrows<Throwable> {
            callable.call(this){
                report = it
            }
        }
        val string = report.stringify().plain
        assertNotNull(report)
        assertTrue { string.contains("Callable can identify source of exception") }
        assertTrue { string.contains("throwingMethod") }
    }
}