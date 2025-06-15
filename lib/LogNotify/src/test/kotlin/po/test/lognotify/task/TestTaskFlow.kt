package po.test.lognotify.task

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TestTaskFlow {


//    val flow = MutableSharedFlow<String>()
//    val messages = mutableListOf<String>()
//
//    val job = launch {
//        flow.collect {
//            messages += it
//        }
//    }
//
//    flow.emit("one")
//    flow.emit("two")
//    job.cancel() // cancel collection
//
//    assertEquals(listOf("one", "two"), messages)

    @Test
    fun `Log flow completes when scope is cancelled`() = runTest {


    }
}