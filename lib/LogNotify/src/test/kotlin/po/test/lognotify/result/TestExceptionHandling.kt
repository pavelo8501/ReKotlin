package po.test.lognotify.result

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.lognotify.TasksManaged
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.common.result.onFailureCause
import po.lognotify.launchers.runAction
import po.lognotify.launchers.runTask
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestExceptionHandling : TasksManaged {
    override val identity: CTXIdentity<out CTX> = asIdentity()

    override val contextName: String = "TestExceptionHandling"

    companion object {
        @JvmStatic
        var subTaskNotExpectingResultHitCount: Int = 0

        @JvmStatic
        var throwingInlineActionHitCount: Int = 0

        @JvmStatic
        var inlineActionThrowingHitCount: Int = 0
    }

    @BeforeEach
    fun dropCounters() {
        subTaskNotExpectingResultHitCount = 0
        throwingInlineActionHitCount = 0
        inlineActionThrowingHitCount = 0
    }

    private fun subTaskThrowing(
        exception: Throwable?,
        outputValue: String = "subTaskThrowing",
    ): String =
        runTask(outputValue) {
            exception?.let { throw it }
            outputValue
        }.resultOrException()

    private fun subTaskSwallowing(
        exception: Throwable,
        outputValue: Int = 10,
    ) = runTask("subTaskSwallowing") {
        throw exception
        outputValue
    }.onFail {
    }

    private fun subTaskNotExpectingResult(
        exception: Throwable,
        shouldThrow: Boolean,
    ): Unit =
        runTask("subTaskNotExpectingResult") {
            if (shouldThrow) {
                throw exception
            }
            subTaskNotExpectingResultHitCount++
            Unit
        }.resultOrException()

    private fun subTaskDefaultProvided(
        exception: Throwable,
        outputValue: Int = 10,
    ): Int =
        runTask("subTaskIntResult") {
            throw exception
            outputValue
        }.handleFailure { exception ->
            10
        }

    private fun inlineActionNotExpectingResult(
        exception: Throwable?,
        name: String = "inlineActionNotExpectingResult",
    ): Unit =
        runAction(name) {
            exception?.let {
                throw it
            }
            throwingInlineActionHitCount++
            Unit
        }

    private fun inlineActionThrowing(
        exception: Throwable?,
        name: String = "inlineActionThrowing",
    ): String =
        runAction(name) {
            exception?.let {
                throw it
            }
            inlineActionThrowingHitCount++
            name
        }

    @Test
    fun `Deeply nested exception reveal thorough trace info`() {
        val exception = Exception("General")
        val managed =
            assertThrows<ManagedException> {
                runTask("EntryTask", TaskConfig(exceptionHandler = HandlerType.SkipSelf, delayMs = 10)) {
                    runTask("subTaskThrowing") {
                        for (i in 1..9) {
                            inlineActionNotExpectingResult(null, "Inline#$i")
                        }
                        inlineActionThrowing(exception)
                        "subTaskThrowing"
                    }
                }
            }
    }

    fun `Skip logic work same and predictable for tasks and inline actions`() {
        val exception = Exception("Should skip")
        val exception2 = Exception("Inline Should Skip")
        assertDoesNotThrow {
            runTask("EntryTask", TaskConfig(exceptionHandler = HandlerType.SkipSelf, delayMs = 10)) {
                subTaskNotExpectingResult(exception, false)
                inlineActionNotExpectingResult(exception2)
                subTaskNotExpectingResult(exception, true)
                inlineActionNotExpectingResult(exception2)
            }
        }
        assertEquals(1, subTaskNotExpectingResultHitCount)
        assertEquals(0, throwingInlineActionHitCount)
    }

    fun `If starting task default handler Cancel_All exception is brought to the entry point`() {
        assertThrows<ManagedException> {
            runTask("EntryTask", TaskConfig(exceptionHandler = HandlerType.CancelAll)) {
                subTaskThrowing(Exception("General"))
            }.onFailureCause {
                throw it
            }
        }
    }

    fun `Exception thrown in sub task root does not handle`() {
        val outerException =
            assertThrows<ManagedException> {
                runTask("RootTask") { subTaskThrowing(Exception("General")) }
            }
        assertNotNull(outerException, "RootTask swallowed exception")
    }

    fun `Exception thrown in sub task and swallowed`() {
        val exception = Exception("General")
        assertDoesNotThrow {
            runTask("RootTask") { subTaskSwallowing(exception, 10) }
        }
    }

    fun `Exception thrown in bottom sub task handled by top sub task root does not throw`() {
        val exception = Exception("General")
        val result =
            assertDoesNotThrow {
                runTask("RootTask") {
                    subTaskDefaultProvided(exception)
                }.resultOrException()
            }
        assertEquals(10, result, "Fallback value does not match")
    }
}
