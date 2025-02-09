package po.lognotify.test.eventhandler

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.shared.enums.HandleType
import po.lognotify.test.testmodels.TestException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEventHandlerCoroutines {

    private lateinit var rootHandler: RootEventHandler
    private lateinit var childHandler: EventHandler
    private val executionCounter = AtomicInteger(0)

    @BeforeEach
    fun setup() {
        rootHandler = RootEventHandler("RootModule")
        childHandler = EventHandler("ChildModule", rootHandler)
        executionCounter.set(0)
    }

//    @Test
//    fun `action executes exactly once`() = runTest {
//        rootHandler.action("Simple Execution Test") {
//            executionCounter.incrementAndGet()
//        }
//        assertEquals(1, executionCounter.get(), "Execution count should be exactly 1")
//    }

//    @Test
//    fun `nested actions execute exactly once`() = runTest {
//        rootHandler.action("Parent Task") {
//            childHandler.action("Child Task") {
//                executionCounter.incrementAndGet()
//            }
//        }
//        assertEquals(1, executionCounter.get(), "Nested execution count should be exactly 1")
//    }

//    @Test
//    fun `exception propagates to parent correctly`() = runTest {
//
//        val exception = assertThrows<TestException> {
//            rootHandler.action("Parent Task") {
//                executionCounter.incrementAndGet()
//                childHandler.action("Child Task") {
//                throw TestException("Simulated Failure", HandleType.PROPAGATE_TO_PARENT)
//                }
//            }
//        }
//
//        assertEquals("Simulated Failure", exception.message)
//        assertEquals(1, executionCounter.get(), "Exception should propagate exactly once")
//    }


//    @Test
//    fun `parallel actions execute only once each`() = runTest {
//        val jobs = List(100) {
//            async {
//                rootHandler.actionAsync("Parallel-Task-$it") { executionCounter.incrementAndGet() }
//            }
//        }
//        jobs.awaitAll()
//        assertEquals(100, executionCounter.get(), "Parallel executions should match exactly")
//    }
//
//    @Test
//    fun `stress test with 1000 events`() = runTest {
//        val jobs = List(1000) {
//            async {
//                rootHandler.actionAsync("HeavyTask-$it") { executionCounter.incrementAndGet() }
//            }
//        }
//        jobs.awaitAll()
//        assertEquals(1000, executionCounter.get(), "Execution count should match number of events")
//    }

//    @Test
//    fun `wipeData clears events`() = runTest {
//        rootHandler.action("First Event") {}
//        assertTrue(rootHandler.eventQue.isNotEmpty(), "Event queue should not be empty before wipe")
//        rootHandler.wipeData()
//        assertTrue(rootHandler.eventQue.isEmpty(), "Event queue should be empty after wipeData()")
//    }

}