package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.TaggedNotifierRegistry
import po.misc.functions.registries.subscribe
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
        val registry = TaggedNotifierRegistry<Events, String>(Events::class.java)


        registry.subscribe(Events.Event1, this){str->
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
        val registry = TaggedNotifierRegistry<Events, String>(Events::class.java)

        registry.subscribe(Events.Event1, 1, this){str->
            received = str
        }

        registry.trigger(Events.Event1, 2L, this::class,  inputValue)
        assertNull(received)

        registry.trigger(Events.Event1, inputValue)
        val receivedValue = assertNotNull(received)
        assertEquals(inputValue, receivedValue, "Value mismatch")
    }


}