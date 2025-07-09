package po.test.lognotify.result

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.lognotify.TasksManaged
import po.lognotify.tasks.models.TaskConfig
import po.lognotify.tasks.result.onFailureCause
import po.lognotify.extensions.runTask
import po.lognotify.extensions.subTask
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.throwManaged
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestExceptionHandling: TasksManaged {


    override val contextName: String = "TestExceptionHandling"

    fun subTaskThrowingManaged(inputParam: Int): Int = subTask("subTaskThrowing"){
        throwManaged("TestException")
    }.resultOrException()

    fun subTaskThrowing(): Int = subTask("subTaskThrowing"){
        throw Exception("GenericException")
        10
    }.resultOrException()

    fun subTaskSwallowing() = subTask("subTaskSwallowing"){
        throw Exception("GenericException")
        10
    }.onFail {

    }

    fun subTaskIntResult(): Int = subTask("subTaskIntResult"){
        subTaskThrowing()
    }.handleFailure{exception->
        10
    }

    @Test
    fun `If starting task default handler Cancel_All exception is brought to the entry point`() {
        assertThrows<ManagedException> {
            runTask("EntryTask", TaskConfig(exceptionHandler = HandlerType.CancelAll)){
                subTaskThrowingManaged(10)
            }.onFailureCause {
                it.throwSelf(this)
            }
        }
    }

    @Test
    fun `Exception thrown in sub task root does not handle`() {
        val outerException = assertThrows<ManagedException> {
            runTask("RootTask") { subTaskThrowing() }
        }
        assertNotNull(outerException, "RootTask swallowed exception")
    }

    @Test
    fun `Exception thrown in sub task and swallowed`() {
        assertDoesNotThrow {
            runTask("RootTask") { subTaskSwallowing() }
        }
    }

    @Test
    fun `Exception thrown in bottom sub task handled by top sub task root does not throw`() {
        val result = assertDoesNotThrow {
            runTask("RootTask") {
                subTaskIntResult()
            }.resultOrException()
        }
        assertEquals(10, result, "Fallback value does not match")
    }

}