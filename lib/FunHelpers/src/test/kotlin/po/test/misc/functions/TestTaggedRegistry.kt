package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.require
import po.misc.functions.registries.subscribe
import po.misc.functions.registries.taggedRegistryOf
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestTaggedRegistry: CTX {

    internal enum class Events {
        OnCreate
    }


    override val identity: CTXIdentity<TestTaggedRegistry> = asIdentity()

    @Test
    fun `Only one subscription is created per class and triggers only for class subscribing`(){

        val inputString = "TestInput"
        val onCreate = taggedRegistryOf<Events, String>(Events.OnCreate)

        var result: String? = null
        for (i in 1.. 10){
            subscribe(onCreate){value->
                result = value
            }
        }

        onCreate.trigger(ControlClass::class, inputString)
        assertEquals(1, onCreate.subscriptionsCount)
        assertNull(result)

        onCreate.trigger(TestTaggedRegistry::class, inputString)
        val resultString = assertNotNull(result)
        assertEquals(inputString, resultString)
    }


    @Test
    fun `Require erases subscription after first trigger`(){

        val inputString1 = "TestInput"
        val inputString2 = "TestInput2"
        var resultString = ""
        val onCreate = taggedRegistryOf<Events, String>(Events.OnCreate)
        require(onCreate){value->
            resultString = value
        }

        onCreate.trigger(inputString1)
        onCreate.trigger(inputString2)
        assertEquals(inputString1, resultString)
        assertEquals(0, onCreate.subscriptionsCount)
    }

    @Test
    fun `Require with receiver of CTX correctly takes its id`(){

        val inputString1 = "TestInput"
        val inputString2 = "TestInput2"
        var result: String? = null
        val onCreate = taggedRegistryOf<Events, String>(Events.OnCreate)

        require(onCreate){value->
            result = value
        }
        onCreate.trigger(this::class, 0L, inputString1)
        assertNull(result)
        onCreate.trigger(this::class, inputString1)
        val resultString = assertNotNull(result)
        assertEquals(inputString1, resultString)

    }
}