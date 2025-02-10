package po.lognotify.test.eventhandler

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.exceptions.PropagateException
import po.lognotify.eventhandler.exceptions.UnmanagedException
import po.lognotify.shared.enums.SeverityLevel
import po.lognotify.test.testmodels.TestSkipException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEventHandler {

    private lateinit var rootHandler: RootEventHandler
    private lateinit var childHandler: EventHandler
    private lateinit var subChildHandler: EventHandler

    private val executionCounter = AtomicInteger(0)

    private var taskExecuted = false
    suspend fun testProcessWithResult(): String {
        val result = "ok"
        return result
    }
    suspend fun testProcessWithNullResult(): String? {
        val result = null
        return result
    }

    fun runningProcess(): Int? {
        var result: Int? = null
        for (i in 1..100) {
            if(i==10){
                throw TestSkipException()
            }
            executionCounter.incrementAndGet()
            result = i
        }
        return result
    }

    @BeforeEach
    fun setup() {
        rootHandler = RootEventHandler("RootModule")
        childHandler = EventHandler("ChildModule", rootHandler)
        subChildHandler = EventHandler("SubChildModule", childHandler)
        taskExecuted = false
    }

    @Test
    fun `task function properly returns result`() = runTest {
        rootHandler.task("No return result") { taskExecuted = true }
        assertTrue(taskExecuted, "No return task executed")

        taskExecuted = false
        val processedFnResult = rootHandler.task("Not null string return") {
            taskExecuted = true
            testProcessWithResult()
        }
        assertEquals("ok", processedFnResult, "testProcessWithResult executed")
        assertTrue(taskExecuted)

        taskExecuted = false
        val processedFnResultFromLambda = rootHandler.task("Not null string return") {
            taskExecuted = true
            val ok = testProcessWithResult()
            ok
        }
        assertEquals("ok", processedFnResultFromLambda, "testProcessWithResult executed with result from lambda")
        assertTrue(taskExecuted)

        taskExecuted = false
        val processedFnResultNull = rootHandler.task("Null return") {
            taskExecuted = true
            testProcessWithNullResult()
        }
        assertNull(processedFnResultNull)
        assertTrue(taskExecuted)

        taskExecuted = false
        val processedFnResultNullFromLambda = rootHandler.task("Null return from lambda") {
            taskExecuted = true
            val resultNull = testProcessWithNullResult()
            resultNull
        }
        assertNull(processedFnResultNullFromLambda)
        assertTrue(taskExecuted)
    }

    @Test
    fun `events registered properly`() = runTest {
        for(parentTaskIndex  in 1..2) {
            rootHandler.task("parent_${parentTaskIndex}") {
                for (childTaskIndex in 1..2) {
                    childHandler.task("child_${childTaskIndex}") {
                        childHandler.info("info_child_${childTaskIndex}")
                        for (subChildTaskIndex in 1..2) {
                            subChildHandler.warn("warn_subChild_${subChildTaskIndex}")
                        }
                    }
                }
            }
        }
        val flatArray =  rootHandler.taskQue.flatMap { it.subEvents }.flatMap { it.subEvents }
        val infos = flatArray.filter { it.type == SeverityLevel.INFO }
        val warnings = flatArray.filter { it.type == SeverityLevel.WARNING }
        assertEquals(2, rootHandler.taskQue.count(), "Expecting 2 events in the root handler")
        assertEquals(
            SeverityLevel.TASK ,
            rootHandler.taskQue[1].type,
            "Expecting second events of type Task in the root handler"
        )
        assertEquals(4, infos.count(), "Expecting 4 events of type INFO")
        assertEquals(8, warnings.count(), "Expecting 8 events of type Warning")
        assertEquals(12, flatArray.count(), "Expecting total flatmap of 12 items")
        warnings.first().let {
            assertEquals("RootModule|ChildModule|SubChildModule", it.module)
            assertEquals("warn_subChild_1", it.msg)
        }
    }

    @Test
    fun `task being skipped by exception but rest flow continues event registered`() = runTest {
        var result: Int? = null
        rootHandler.task("parentLoop") {
            for (i in 1..100) {
                childHandler.task("childLoop") {
                    if (i == 10) { throw TestSkipException() }
                    executionCounter.incrementAndGet()
                    result = i
                }
            }
        }
        assertEquals(99, executionCounter.get())
        assertEquals(100, result)
        assertEquals(1, rootHandler.taskQue.count())
        assertNull(childHandler.activeTask)
    }

    @Test
    fun `exception propagated to the top but unhandled`() = runTest {
        assertThrows<UnmanagedException>{
            rootHandler.task("parent") {
                childHandler.task("child") {
                   subChildHandler.task("subChild") {
                       throw PropagateException("propagate exception")
                   }
                }
            }
        }
    }

    @Test
    fun `generic exception rethrown as unmanaged` () = runTest {
        assertThrows<UnmanagedException>{
            rootHandler.task("parent") {
                childHandler.task("child") {
                    subChildHandler.task("subChild") {
                        @Suppress("TooGenericExceptionThrown")
                        throw RuntimeException("runtimeException exception")
                    }
                }
            }
        }
    }

    @Test
    fun `if propagated thrown and handler exists` () = runTest {
        var propagatedHandled = false
        rootHandler.task("parent") {
            rootHandler.onPropagateException{
                propagatedHandled = true
            }
            childHandler.task("child") {
                subChildHandler.task("subChild") {
                    throw PropagateException("propagate exception")
                }
            }
        }
        assertTrue(propagatedHandled)
    }

}