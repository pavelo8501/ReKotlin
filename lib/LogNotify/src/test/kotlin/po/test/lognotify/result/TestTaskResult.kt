package po.test.lognotify.result

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import po.lognotify.TasksManaged
import po.lognotify.common.result.TaskResult
import po.lognotify.common.result.resultOrNull
import po.lognotify.extensions.runTaskAsync
import po.lognotify.interfaces.FakeTasksManaged
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestTaskResult: FakeTasksManaged {

    override val contextName: String
        get() = "TestTaskResult"

    suspend fun nonNullable(name: String, value: Int): Int = runTaskAsync(name) {
        value
    }.resultOrException()

    suspend fun nullableResult(name: String, value: Int?): Int? = runTaskAsync(name) {
        value
    }.resultOrException()

    suspend fun nullableResultResultOrNull(name: String, value: Int?): Int? = runTaskAsync(name) {
        value
    }.resultOrNull()

    @Test
    fun `Container returns result as expected`() = runTest {

        val result = nonNullable(name = "task1", value = 10)
        assertEquals(10, result, "Result mismatch")

        assertThrows<Throwable> {
            val nullInt : Int? = null
            nonNullable(name = "task2", value = nullInt!!)
        }

        var swallowedException : Throwable? = null
        assertDoesNotThrow {
            val result1 = nullableResult(name = "task2", value = null)
            val result2 = nullableResultResultOrNull(name = "NullableResultResult", value = null)
            assertAll(
                "Both nullable results received",
                { assertNull(result1) },
                { assertNull(result2) },
            )
            runTaskAsync("Exception handled"){
                throw IllegalArgumentException("should_swallow")
            }.onFail {
                swallowedException = it
            }

            var onResultTriggered : Boolean = false
            runTaskAsync("OnResult does not trigger on nullable"){
                val nullableResult : Int? = null
                nullableResult
            }.onResult { result->
                onResultTriggered = true
            }
            assertFalse(onResultTriggered, "Nullable result does not trigger onResult")

            var triggeredAndResulted  = 0
            runTaskAsync("PositiveResult") {
                123
            }.onResult {
                triggeredAndResulted = it
            }
            assertEquals(123, triggeredAndResulted, "OnResult returned wrong value")

            var onCompleteResult: TaskResult<*>? = null
            runTaskAsync("OnComplete"){
                val nullableResult : Int? = null
                nullableResult
            }.onComplete { result->
                onCompleteResult = result
            }
            assertAll("OnComplete triggers on nullable, not throwing",
                { assertNotNull(onCompleteResult, "onResultTriggered never reached") },
                { assertInstanceOf<TaskResult<Int?>>(onCompleteResult, "onComplete returned wrong result") },
                { assertTrue(onCompleteResult is TaskResult<*>, "onComplete returned wrong result") }
             )
        }
        val exception =  assertNotNull(swallowedException, "onFail never reached")
        assertEquals("IllegalArgumentException: should_swallow", exception.message)
    }

}