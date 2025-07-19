package po.test.lognotify.task

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.TasksManaged
import po.lognotify.extensions.runInlineAction
import po.lognotify.extensions.runTask
import po.lognotify.tasks.models.TaskConfig
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.test.lognotify.result.TestExceptionHandling.Companion.inlineActionThrowingHitCount
import po.test.lognotify.result.TestExceptionHandling.Companion.throwingInlineActionHitCount
import kotlin.test.assertEquals

class TestAuxMethods() : TasksManaged {

    override val contextName: String = "TestAuxMethods"

    private fun inlineActionThrowing(
        exception: Throwable?,
        name: String = "inlineActionThrowing"
    ): String = runInlineAction(name)
    {
        exception?.let {
            throw it
        }
        inlineActionThrowingHitCount ++
        name
    }

    private fun inlineActionNotExpectingResult(
        exception: Throwable?,
        name: String = "inlineActionNotExpectingResult"
    ): Unit = runInlineAction(name)
    {
        exception?.let {
            throw it
        }
        throwingInlineActionHitCount ++
        Unit
    }

    @Test
    fun `Hierarchy members correctly provide backtrace data`() {
        val exception = Exception("General")
        val managed = assertThrows<ManagedException> {
            runTask("EntryTask", TaskConfig(exceptionHandler = HandlerType.SkipSelf, delayMs = 10)) {
                runTask("subTaskThrowing"){
                    for (i in 1..9) {
                        inlineActionNotExpectingResult(null, "Inline#${i}")
                    }
                    printHierarchy()
                    inlineActionThrowing(null)
                    "subTaskThrowing"
                }
            }
        }
        val backTrace = managed.handlingData.flatMap { it.events.items }.flatMap { it.backTraceRecords }
        assertEquals(1, backTrace.size)
        backTrace.forEach { it.echo() }
    }


}