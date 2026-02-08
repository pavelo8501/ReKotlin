package po.test.misc.debugging.stack_tracer

import po.misc.debugging.stack_tracer.StackFrameMeta
import po.misc.exceptions.Tracer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestStackFrameMeta {

    val meta : StackFrameMeta = Tracer().trace.bestPick

    @Test
    fun `StackFrameMeta output without console link included`(){
        val render = meta.formatted()
        assertTrue { render.contains("Method name") }
        assertFalse { render.contains("Console link") }
    }

    @Test
    fun `Output with console link`(){
        val render = meta.formatted(StackFrameMeta.Template.ConsoleLink)
        assertTrue { render.contains("Method name") }
        assertTrue { render.contains("Console link") }
    }

}