package po.test.misc.callback

import org.junit.jupiter.api.Test
import po.misc.callbacks.CallbackManager
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TestCallbackDataHandling: IdentifiableClass {

    override val identity = asIdentifiableClass("TestCallbackDataHandling", "Test")

    class FirstHoldingClass: IdentifiableContext{
        enum class Event{ OnInit, OnRouted  }
        override val contextName: String = "FirstHoldingClass"
        val notifier = CallbackManager(enumClass = Event::class.java, emitter = this)
        val initialPayload = CallbackManager.createPayload<Event, Int>(notifier, Event.OnInit)
        val routedPayload = CallbackManager.createPayload<Event, String>(notifier, Event.OnRouted)
    }

    class SecondHoldingClass: IdentifiableContext {
        enum class Event{ OnData }
        override val contextName: String = "SecondHoldingClass"
        val notifier = CallbackManager(enumClass = Event::class.java, emitter = this)
        val dispatcherPayload = CallbackManager.createPayload<Event, String>(notifier, Event.OnData)
    }

    @Test
    fun `Routing work as expected`(){
        val firstHolder = FirstHoldingClass()
        val firstManager = firstHolder.notifier

        val secondHolder = SecondHoldingClass()
        val dataEmittedBySecondHoldingClass = "Test string"
        var dataReceived: Any? = null

        firstManager.subscribe<String>(this, FirstHoldingClass.Event.OnRouted){
            dataReceived = it.getData()
        }
        firstManager.bridge(secondHolder.dispatcherPayload, firstHolder.routedPayload)
        secondHolder.dispatcherPayload.triggerForAll(dataEmittedBySecondHoldingClass)

        val data =  assertIs<String>(dataReceived)
        assertEquals(dataEmittedBySecondHoldingClass, data, "Wrong string received")
    }

    @Test
    fun `Different subscription types work as expected`(){
        val holder = FirstHoldingClass()
        val manager = holder.notifier
        var intTriggersCount = 0
        var stringTriggersCount = 0
        manager.subscribe<Int>(this, FirstHoldingClass.Event.OnInit){
            intTriggersCount++
        }
        manager.subscribe<String>(this, FirstHoldingClass.Event.OnRouted){
            stringTriggersCount++
        }
        for(i in 1..4){
            holder.initialPayload.triggerForAll(1)
            holder.routedPayload.triggerForAll("String")
        }
        holder.routedPayload.triggerForAll("Additional")
        assertEquals(4, intTriggersCount)
        assertEquals(5, stringTriggersCount)
    }
}