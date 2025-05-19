package po.test.misc.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import po.misc.coroutines.CoroutineHolder
import po.misc.coroutines.RunAsync
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals

class TestCoroutines {


    class CoroutineConsumer(override val coroutineContext: CoroutineContext) : CoroutineHolder {
        val coroutineName get() = coroutineContext[CoroutineName]?.name ?: "none"
    }


    fun defaultScope(name: String): CoroutineContext {
        val context = SupervisorJob() + Dispatchers.Default + CoroutineName(name)
        return context
    }

    @Test
    fun `Launcher is using correct scope`() {

        val coroutineName = "some_name"
        val consumer = CoroutineConsumer(defaultScope(coroutineName))

        val result = runBlocking {
            consumer.RunAsync {
                val inScope = coroutineContext[CoroutineName]?.name ?: "unavailable"
                println(inScope)
                inScope
            }
        }

        assertEquals("some_name", consumer.coroutineName, "Coroutine name is not assigned")
        assertEquals(consumer.coroutineName, result, "Context is not applied to consumer")
    }

}