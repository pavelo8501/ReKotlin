package po.test.lognotify.task

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.common.configuration.TaskConfig
import po.lognotify.launchers.runAction
import po.lognotify.launchers.runTask
import po.test.lognotify.setup.FakeTasksManaged
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.test.lognotify.result.TestExceptionHandling.Companion.inlineActionThrowingHitCount
import po.test.lognotify.result.TestExceptionHandling.Companion.throwingInlineActionHitCount

class TestAuxMethods : FakeTasksManaged {
    override val contextName: String = "TestAuxMethods"

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

    @Test
    fun `Hierarchy members correctly provide backtrace data`() {
        val exception = Exception("General")
        val managed =
            assertThrows<ManagedException> {
                runTask("EntryTask", TaskConfig(exceptionHandler = HandlerType.SkipSelf, delayMs = 10)) {
                    runTask("subTaskThrowing") {
                        for (i in 1..9) {
                            inlineActionNotExpectingResult(null, "Inline#$i")
                        }
                        inlineActionThrowing(null)
                        "subTaskThrowing"
                    }
                }
            }
    }
}
