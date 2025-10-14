package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.functions.registries.addHook
import po.misc.functions.registries.buildSubscriptions
import po.misc.functions.registries.models.TaggedSubscriber
import po.misc.functions.registries.builders.require
import po.misc.functions.registries.builders.subscribe
import po.misc.functions.registries.builders.taggedRegistryOf
import po.misc.types.token.TypeToken
import po.misc.types.type_data.TypeData
import po.test.misc.setup.ControlClass
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestCallbackRegistryBase: CTX {

    internal enum class TestEvents {
        OnCreate,
        OnSomething
    }


    override val identity: CTXIdentity<TestCallbackRegistryBase> = asIdentity()

    @Test
    fun `Only one subscription is created per class and triggers only for class subscribing`() {
        var result: String? = null
        val inputString = "TestInput"
        val onCreate = taggedRegistryOf<TestEvents, String>(TestEvents.OnCreate){


        }
        for (i in 1..10) {
            subscribe(onCreate) { value ->
                result = value
            }
        }



        onCreate.trigger(ControlClass::class, inputString)
        assertEquals(1, onCreate.subscriptionsCount)
        assertNull(result)

        onCreate.trigger(TestCallbackRegistryBase::class, inputString)



        val resultString = assertNotNull(result)
        assertEquals(inputString, resultString)
    }


    @Test
    fun `Require erases subscription after first trigger`() {

        val inputString1 = "TestInput"
        val inputString2 = "TestInput2"
        var resultString = ""
        val onCreate = taggedRegistryOf<TestEvents, String>(TestEvents.OnCreate)

        require(onCreate) { value ->
            resultString = value
        }

        onCreate.trigger(inputString1)
        onCreate.trigger(inputString2)
        assertEquals(inputString1, resultString)
        assertEquals(0, onCreate.subscriptionsCount)
    }

    @Test
    fun `Require with receiver of CTX correctly takes its id`() {

        val inputString1 = "TestInput"
        val inputString2 = "TestInput2"
        var result: String? = null
        val onCreate = taggedRegistryOf<TestEvents, String>(TestEvents.OnCreate)

        require(onCreate) { value ->
            result = value
        }
        onCreate.trigger(TestEvents.OnCreate,  this::class, 0L, inputString1)

        assertNull(result)
        onCreate.trigger(this::class, inputString1)
        val resultString = assertNotNull(result)
        assertEquals(inputString1, resultString)

    }



    @Test
    fun `Subscription pack builder`() {

        val hooks = buildSubscriptions<String>(TypeToken.create<TestCallbackRegistryBase>()) {
            addHook(TestEvents.OnCreate, oneShot = false){}
            addHook(TestEvents.OnSomething, oneShot = true){}
        }
        assertEquals(2, hooks.subscriptions.size)
        val firstHook = assertNotNull(hooks.subscriptions.firstOrNull()?.first)
        assertEquals(false, firstHook.requireOnce)
        val firstSubscription = assertIs<TaggedSubscriber<*>>(firstHook)
        assertEquals(TestEvents.OnCreate,  firstSubscription.enumTag)

        val secondHook = assertNotNull(hooks.subscriptions.lastOrNull()?.first)
        assertEquals(true, secondHook.requireOnce)
        val secondSubscription = assertIs<TaggedSubscriber<*>>(secondHook)
        assertEquals(TestEvents.OnSomething,  secondSubscription.enumTag)
    }

    @Test
    fun `Subscription pack binding to EmitterAwareRegistry`() {
        val hooks = buildSubscriptions<String>(TypeToken.create<TestCallbackRegistryBase>()) {
            addHook(TestEvents.OnCreate, oneShot = false){}
            addHook(TestEvents.OnSomething, oneShot = true){}
        }
        val registry = taggedRegistryOf<TestEvents, String>()
        registry.trySubscribe(hooks)
        assertEquals(2, registry.subscriptionsCount)
        assertEquals(1, registry.requireOnceCount)
        assertEquals(1, registry.permanentCount)
    }


}