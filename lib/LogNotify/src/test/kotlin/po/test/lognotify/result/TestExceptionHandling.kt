package po.test.lognotify.result

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.lognotify.TasksManaged
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.runTask
import po.lognotify.extensions.subTask
import po.misc.exceptions.ManagedException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestExceptionHandling: TasksManaged {

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