package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.subscribers.TaggedLambdaRegistry
import po.misc.functions.subscribers.subscribe
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestLambdaRegistry: CTX {
    internal enum class Events{
        Event1
    }

    override val identity: CTXIdentity<TestLambdaRegistry> = asIdentity()

   init {
       identity.setId(10)
   }


    @Test
    fun `Subscription by class and id work as expected`(){

        val inputValue = "Input"
        var received: String? = null
        val registry = TaggedLambdaRegistry<Events, String>()


        registry.subscribe(this, Events.Event1){str->
            received = str
        }

        registry.trigger(Events.Event1, inputValue)

        val receivedValue = assertNotNull(received, "Callback never triggered")
        assertEquals(inputValue, receivedValue, "Value mismatch")
    }

    @Test
    fun `Subscription by class and id`(){

        val inputValue = "Input2"
        var received: String? = null
        val registry = TaggedLambdaRegistry<Events, String>()

        registry.subscribe(this, Events.Event1){str->
            received = str
        }

        registry.trigger(1, Events.Event1, inputValue)
        assertNull(received)

        registry.trigger(10, Events.Event1, inputValue)
        val receivedValue = assertNotNull(received)
        assertEquals(inputValue, receivedValue, "Value mismatch")

    }


}