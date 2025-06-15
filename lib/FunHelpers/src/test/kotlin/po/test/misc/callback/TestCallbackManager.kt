package po.test.misc.callback

import org.junit.jupiter.api.Test
import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.Containable
import po.misc.callbacks.manager.CallbackPayload
import po.misc.callbacks.manager.bridgeFrom
import po.misc.callbacks.manager.callbackManager
import po.misc.callbacks.manager.listen
import po.misc.callbacks.manager.managerHooks
import po.misc.callbacks.manager.payload
import po.misc.callbacks.manager.requestOnce
import po.misc.callbacks.manager.withCallbackManager
import po.misc.callbacks.manager.withPayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.managedException
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass
import po.test.misc.callback.TestCallbackManager.FirstHoldingClass.Event
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestCallbackManager() : IdentifiableClass {

    override val contextName: String = "TestExperimentalCallbackManager"
    override val identity = asIdentifiableClass("Test")
    enum class HostEvent{ Unused, UnusedBuilder, UnusedSelfSubscribed }

    class FirstHoldingClass: IdentifiableContext{
        enum class Event{ OnInit }
        override val contextName: String = "HoldingClass"

        val notifier = CallbackManager(
            enumClass = Event::class.java,
            emitter = this,
           CallbackPayload<Event, Int>(Event.OnInit)
        )
    }

    class SecondHoldingClass: IdentifiableClass {
        enum class Event{ OnRouted }

        override val identity = asIdentifiableClass("SecondHoldingClass")

        val notifier = CallbackManager(
            enumClass = Event::class.java,
            emitter = this,
            CallbackPayload<Event, String>(Event.OnRouted)
        )

        fun trigger(data: String){
            notifier.trigger(Event.OnRouted, data)
        }
    }

    @Test
    fun `Callback manager hooks reflect true info`(){
        val holder = FirstHoldingClass()
        lateinit var receivedContainer:  Containable<Int>
        var beforeTriggerSubscriberName: String = ""
        var beforeTriggerEventName : String = ""

        var newSubscriptionEmitterName: String = ""
        var beforeTriggerEmitterName: String = ""
        var afterTriggeredEmitterName: String = ""

        holder.notifier.managerHooks {
            beforeTrigger{
                beforeTriggerSubscriberName =  it.subscriber.completeName
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
        holder.notifier.subscribe<Int>(this, FirstHoldingClass.Event.OnInit){
            receivedContainer = it
        }

        holder.notifier.trigger(this, FirstHoldingClass.Event.OnInit, 24)

        assertNotNull(holder.notifier.hooks, "managerHooks extension do not install hooks")
        assertEquals(completeName, beforeTriggerSubscriberName)
        assertEquals("OnInit", beforeTriggerEventName)
        assertEquals(holder.contextName, newSubscriptionEmitterName)
        assertEquals(newSubscriptionEmitterName, beforeTriggerEmitterName)
        assertEquals(beforeTriggerEmitterName, afterTriggeredEmitterName, "afterTriggeredEmitterName does not match")
        assertEquals(completeName,  receivedContainer.subscriber.completeName)
    }

    @Test
    fun `Different subscription types work as expected`(){

        val holder = FirstHoldingClass()
        var callbackTriggersCount = 0
        holder.notifier.subscribe<Int>(this, FirstHoldingClass.Event.OnInit){
            callbackTriggersCount++
        }

        for(i in 1..4){
            holder.notifier.trigger(FirstHoldingClass.Event.OnInit, 1)
        }
        assertEquals(4, callbackTriggersCount)
        callbackTriggersCount =0
        holder.notifier.unsubscribeAll()


        holder.notifier.request<Int>(this, FirstHoldingClass.Event.OnInit){
            callbackTriggersCount++
        }
        for(i in 1..4){
            holder.notifier.trigger(FirstHoldingClass.Event.OnInit, 1)
        }
        assertEquals(1, callbackTriggersCount)

    }

    @Test
    fun `One parameter callback manager data forwarding`(){

        val firstHolder = FirstHoldingClass()
        val secondHolder = SecondHoldingClass()
        val stringSent = "Some Strings"

        var received: Any? = null
        lateinit var subscriber: IdentifiableClass

        secondHolder.notifier.managerHooks {
            forwarding {
                println(it)
            }
        }

        firstHolder.notifier.subscribe<Int>(this, Event.OnInit){
            subscriber = it.subscriber
            received = it.getData()
        }

        val secondPayload = secondHolder.notifier.payload<String>(SecondHoldingClass.Event.OnRouted)

        firstHolder.notifier.bridge<String, Int>(
            sourcePayload = secondPayload,
            eventType = FirstHoldingClass.Event.OnInit,
            dataAdapter = { it.count() }
        )
        secondHolder.trigger(stringSent)

        val dataReceived = assertNotNull(received)
        val intData: Int = assertIs<Int>(dataReceived)
        assertEquals(stringSent.count(), intData)
        assertEquals(completeName, subscriber.completeName, "Subscriber received by callback is not this module")
    }

    @Test
    fun `Helper functions work as intended`(){

        val callbackManager1 = callbackManager<HostEvent>(
            CallbackPayload<HostEvent, Int>(HostEvent.Unused),
            CallbackPayload<HostEvent, String>(HostEvent.Unused)
        )
        val callbackManager2 = callbackManager(
            HostEvent.UnusedBuilder,
            { payload<HostEvent, String>() },
            { payload<HostEvent, Boolean>() }
        )

        val callbackManager3 = withCallbackManager<HostEvent> {
            listen<HostEvent, String>(this@TestCallbackManager, HostEvent.UnusedSelfSubscribed){}
            listen<HostEvent, Int>(this@TestCallbackManager, HostEvent.UnusedSelfSubscribed){}
            requestOnce<HostEvent, Int>(this@TestCallbackManager, HostEvent.UnusedSelfSubscribed){}
        }
        val manger1Stats =  callbackManager1.getStats()
        val manger2Stats =  callbackManager2.getStats()
        val manger3Stats =  callbackManager3.getStats()

        assertEquals(1, manger1Stats.eventTypesCount, "manger1 event count mismatch")
        assertEquals(2, manger1Stats.subscriptionsCount, "manger1 subscriptions count mismatch")

        assertEquals(1, manger2Stats.eventTypesCount, "manger2 event count mismatch")
        assertEquals(2, manger2Stats.subscriptionsCount, "manger2 subscriptions count mismatch")

        assertEquals(1, manger3Stats.eventTypesCount, "manger3 event count mismatch")
        assertEquals(3, manger3Stats.subscriptionsCount, "manger3 subscriptions count mismatch")

        val firstNotifier = FirstHoldingClass().notifier

        val secondNotifier = SecondHoldingClass().withCallbackManager<SecondHoldingClass.Event> {
            val sourcePayload = firstNotifier.getPayload<Int>(Event.OnInit)
            withPayload<SecondHoldingClass.Event, String>(SecondHoldingClass.Event.OnRouted){
                bridgeFrom(sourcePayload) {
                    it.chars().count().toInt()
                }
            }
        }


    }

}