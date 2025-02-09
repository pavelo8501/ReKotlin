package po.lognotify.test.eventhandler

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.test.testmodels.TestSkipException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEventHandler {


    private lateinit var rootHandler: RootEventHandler
    private lateinit var childHandler: EventHandler
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
        taskExecuted = false
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



}