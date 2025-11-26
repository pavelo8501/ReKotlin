package po.test.misc.exceptions

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import po.misc.data.logging.ContextAware
import po.misc.exceptions.handling.delegateIfThrow
import po.misc.exceptions.handling.registerHandler
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestExceptionHandling() : ContextAware {


    @Test
    fun `Non returnable lambda work as expected`() = runTest {

        var message: String? = null
        registerHandler<Exception> { forwardedException ->
            message = forwardedException.message
            throw forwardedException
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