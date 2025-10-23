package po.test.misc.exceptions

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.logging.ContextAware
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.exceptions.createTrace
import po.misc.exceptions.handling.delegateIfThrow
import po.misc.exceptions.handling.registerHandler
import po.misc.exceptions.metaFrameTrace
import po.misc.exceptions.stack_trace.ExceptionTrace
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


fun Throwable.getTrace():  ExceptionTrace{
  return  createTrace(TestExceptionHandling::class)
}

fun Throwable.getTrace(block: Throwable.()-> Unit){
    this.block()
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestExceptionHandling() : ContextAware {


    @Test
    fun `Check if trace can be obtained from wide generic exceptions`() {

        val exception = assertThrows<Exception> {
            try {
                throw Exception("Some text")
            } catch (th: Throwable) {
                th.getTrace {
                    metaFrameTrace(TestExceptionHandling::class)?.let {
                        it.output()
                        it.stackFrames.output {
                            it.colorize(Colour.Green)
                        }
                    }
                }
            }
        }

        val result = exception.getTrace()
        assertIs<TestExceptionHandling>(result.kClass)
        assertEquals(3, result.stackFrames.size)

    }


    @Test
    fun `Non returnable lambda work as expected`() = runTest {

        var message: String? = null
        try {
            registerHandler<Exception> { forwardedException ->
                message = forwardedException.message
                throw forwardedException
            }
        } catch (th: Throwable) {
            th.output { "Throwable from catch block :" + it }
        }

        assertThrows<Exception> {
            delegateIfThrow {
                throw Exception("Thrown by objectDelegating")
            }
        }
        val exceptionMessage = assertNotNull(message)
        assertTrue { exceptionMessage.contains("Thrown by objectDelegating") }
    }
}