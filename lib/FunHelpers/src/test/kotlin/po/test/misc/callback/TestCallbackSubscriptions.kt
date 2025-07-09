package po.test.misc.callback

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.Containable
import po.misc.callbacks.builders.callbackManager
import po.misc.callbacks.builders.listen
import po.misc.callbacks.builders.managerHooks
import po.misc.callbacks.builders.requestOnce
import po.misc.callbacks.builders.withCallbackManager
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass
import po.test.misc.callback.TestCallbackSubscriptions.FirstHoldingClass.Event
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestCallbackSubscriptions() : IdentifiableClass {

    override val contextName: String = "TestCallbackSubscriptions"
    override val identity = asIdentifiableClass("TestCallbackDataHandling", "Test")

    class FirstHoldingClass: IdentifiableContext{
        enum class Event{ OnInit, OnOneShot }
        override val contextName: String = "FirstHoldingClass"

        val notifier = CallbackManager(
            enumClass = Event::class.java,
            emitter = this)

        val intPayload =  CallbackManager.createPayload<Event, Int>(notifier, Event.OnInit)
        val booleanPayload = CallbackManager.createPayload<Event, Boolean>(notifier, Event.OnInit)
        val intOneShotPayload =  CallbackManager.createPayload<Event, Int>(notifier, Event.OnOneShot)

    }
    @Test
    fun `Subscriptions work as expected`(){
        val manager = FirstHoldingClass().notifier
        manager.subscribe<Int>(this, Event.OnInit){}
        manager.subscribe<Boolean>(this, Event.OnInit){}
        manager.request<Int>(this, Event.OnOneShot){}
        val info = manager.getStats()
        assertEquals(3, info.subscriptionsCount, "Subscription count mismatch")
    }

    @Test
    fun `DSL type subscriptions  work as expected`(){
        val manager = callbackManager<Event>(
            { CallbackManager.createPayload<Event, Int>(this, Event.OnInit) },
            { CallbackManager.createPayload<Event, Boolean>(this, Event.OnInit) } ,
            { CallbackManager.createPayload<Event, Int>(this, Event.OnOneShot) }
        )
        var managerInfo = manager.getStats()
        assertEquals(2, managerInfo.eventTypesCount, "Registered event count should be 1")
        assertEquals(3, managerInfo.payloadsCount, "Payloads count should be 3")

        assertDoesNotThrow {
            withCallbackManager(manager){
                listen<Event, Int>(Event.OnInit){}
                listen<Event, Boolean>(Event.OnInit){}
                requestOnce<Event, Int>(Event.OnOneShot){}
            }
        }
        managerInfo = manager.getStats()
        assertEquals(3, managerInfo.subscriptionsCount, "Registered subscriptions count should be 3")
    }

    @Test
    fun `Hooks reflect true info`(){
        val holder  = FirstHoldingClass()
        val manager = holder.notifier
        lateinit var receivedContainer:  Containable<Int>
        var beforeTriggerSubscriberName: String = ""
        var beforeTriggerEventName : String = ""

        var newSubscriptionEmitterName: String = ""
        var beforeTriggerEmitterName: String = ""
        var afterTriggeredEmitterName: String = ""

        manager.managerHooks {
            beforeTrigger{
                beforeTriggerSubscriberName =  it.subscriber.contextName
                beforeTriggerEventName = it.eventType.name
                beforeTriggerEmitterName = it.emitter.contextName
            }
            afterTriggered{
                afterTriggeredEmitterName =  it.emitter.contextName
            }
            newSubscription{
                newSubscriptionEmitterName = it.emitter.contextName
            }
        }
        manager.subscribe<Int>(this, Event.OnInit){
            receivedContainer = it
        }
        manager.trigger(Event.OnInit,  holder.intPayload.typeKey, 20)
        assertNotNull(manager.hooks, "managerHooks extension do not install hooks")
        assertEquals(completeName, beforeTriggerSubscriberName)
        assertEquals("OnInit", beforeTriggerEventName)
        assertEquals(manager.sourceName, newSubscriptionEmitterName, "Wrong callback manager context name")
        assertEquals(newSubscriptionEmitterName, beforeTriggerEmitterName)
        assertEquals(beforeTriggerEmitterName, afterTriggeredEmitterName, "afterTriggeredEmitterName does not match")
        assertEquals(completeName,  receivedContainer.subscriber.contextName)
    }
}