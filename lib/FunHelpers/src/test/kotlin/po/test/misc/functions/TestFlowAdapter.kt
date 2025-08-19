package po.test.misc.functions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.functions.containers.FlowAdapter
import po.misc.functions.containers.then

class TestFlowAdapter {

    val step1 = FlowAdapter<String, Int> { it.length }

    val step2 = FlowAdapter<Int, Boolean> { it > 5 }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `flow adapter`() = runTest {

        val pipeline = step1.output.flatMapLatest {
            step2.emitData(it)
            step2.output
        }

        launch {
            pipeline.collect { println("Result: $it") }
        }
        step1.emitData("Hello world")
    }

    @Test
    fun `flow adapter infix composition`() = runTest {

        val composed = step1 then step2
        launch {
            composed.collect { println("Result: $it") }
        }
        step1.emitData("Hello world")
    }
}