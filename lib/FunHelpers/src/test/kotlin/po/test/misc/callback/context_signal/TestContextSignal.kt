package po.test.misc.callback.context_signal

import po.misc.callbacks.context_signal.ContextSignal
import po.misc.callbacks.context_signal.contextSignalOf
import po.misc.functions.LambdaOptions
import po.misc.functions.NoParam
import po.misc.functions.NoResult
import kotlin.test.Test
import kotlin.test.assertEquals

class TestContextSignal {

    private val signal: ContextSignal<TestContextSignal, Unit, Unit> = contextSignalOf<TestContextSignal>(NoParam, NoResult)

    @Test
    fun `Context signal promise(call once) work as expected`(){

        var triggerCount = 0

        signal.onSignal(LambdaOptions.Promise){
            triggerCount++
        }
        repeat(4){
            signal.trigger(this)
        }
        assertEquals(1, triggerCount)
    }

}