package po.test.misc.debugging.stack_tracer

import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.debugging.toFrameMeta
import po.misc.exceptions.Tracer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestStackFrameMeta {


    @Test
    fun `StackFrameMeta output template`(){
        val meta : StackFrameMeta = Tracer().firstTraceElement.toFrameMeta()
        val render1 = meta.formatted()
        assertTrue { render1.contains("Method name") }
        assertFalse { render1.contains("Console link") }
        val render2 = meta.formatted(StackFrameMeta.Template.ConsoleLink)
        assertTrue { render2.contains("Method name") && render2.contains("Console link") }
    }

}