package po.test.misc.debugging.controller

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.styles.Colour
import po.misc.debugging.controller.DebugController
import po.misc.types.k_class.simpleOrAnon


interface DebugEnabled{
    val controller : DebugController<*>
}

fun <T: Any, R> DebugEnabled.startDebug(listener: T, block: T.(DebugController<*>)-> R): R{
    val receiverClass = listener::class
    "DebugEnabledClass invoked lambda with receiver ${receiverClass.simpleOrAnon}".output(Colour.GreenBright)
    return  block(listener,  controller)
}

class TestDebugController {

    class DebugEnabledClass(): DebugEnabled {
        override val controller = DebugController<DebugEnabledClass>(this)
    }

    @Test
    fun `Controller relaunches outer lambda`(){

        val debugEnabledClass = DebugEnabledClass()

        val handler =  debugEnabledClass.startDebug(this){controller->
            10
        }
    }
}