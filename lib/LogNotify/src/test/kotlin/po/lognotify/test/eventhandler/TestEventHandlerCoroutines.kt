package po.lognotify.test.eventhandler

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class TestEventHandlerCoroutines {
//
//    private lateinit var rootHandler: RootEventHandler
//    private lateinit var childHandler: EventHandler
//    private val executionCounter = AtomicInteger(0)
//
//    @BeforeEach
//    fun setup() {
//        rootHandler = RootEventHandler("RootModule"){
//
//        }
//        childHandler = EventHandler("ChildModule", rootHandler)
//        executionCounter.set(0)
//    }
//
//    @Test
//    fun `action executes exactly once`() = runTest {
//        rootHandler.task("Simple Execution Test") {
//            executionCounter.incrementAndGet()
//        }
//        assertEquals(1, executionCounter.get(), "Execution count should be exactly 1")
//    }
//
//    @Test
//    fun `nested actions execute exactly once`() = runTest {
//        rootHandler.task("Parent Task") {
//            childHandler.task("Child Task") {
//                executionCounter.incrementAndGet()
//            }
//        }
//        assertEquals(1, executionCounter.get(), "Nested execution count should be exactly 1")
//    }
//
//    @Test
//    fun `parallel actions execute only once each`() = runTest {
//        val jobs = List(100) {
//            async {
//                rootHandler.task("Parallel-Task-$it") { executionCounter.incrementAndGet() }
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
//                rootHandler.task("HeavyTask-$it") { executionCounter.incrementAndGet() }
//            }
//        }
//        jobs.awaitAll()
//        assertEquals(1000, executionCounter.get(), "Execution count should match number of events")
//    }
//
//    @Test
//    fun `wipeData clears events`() = runTest {
//        rootHandler.task("First Event") {}
//        assertTrue(rootHandler.taskQue.isNotEmpty(), "Event queue should not be empty before wipe")
//        rootHandler.wipeData()
//        assertTrue(rootHandler.taskQue.isEmpty(), "Event queue should be empty after wipeData()")
//    }
//
//}