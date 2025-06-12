package po.test.misc.callback

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import po.misc.callbacks.CallbackManager

import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.ResultCallbackPayload
import po.misc.callbacks.callbackManager
import po.misc.callbacks.experimental.CallableContainer
import po.misc.callbacks.experimental.ExpEvent
import po.misc.callbacks.experimental.ExperimentalCallbackManager
import po.misc.callbacks.experimental.ExperimentalPayload
import po.misc.callbacks.experimental.ExperimentalPayloadBase
import po.misc.callbacks.experimental.ExperimentalResultPayload
import po.misc.callbacks.resultCallbackManager
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestCallbackManager {

    enum class Event(override val value: Int): ValueBased{
        Event1(1),
        Event2(2),
        Event3(3),
        Event4(4),
    }

    val thisIdentifiable : Identifiable = asIdentifiable("SomeName", "TestCallbackManager")
    val callbackManager =  callbackManager<String>()
    val callbackManager2 =  callbackManager<String>()
    val callbackManagerWithReturn =  resultCallbackManager<String, Boolean>()


    @Test
    fun `One parameter callback manager usage`(){
        var updatedStr: String = ""
        val newValue = "New Value"
        callbackManager.subscribe(thisIdentifiable, CallbackPayload.create(Event.Event1){
            updatedStr = it
        })
        callbackManager.trigger(thisIdentifiable, Event.Event1, newValue)


        assertEquals(newValue, updatedStr)
    }

    @Test
    fun `One parameter callback manager with result usage`(){
        var updatedStr  = ""
        val newValue = "New Value"
        callbackManagerWithReturn.subscribe(thisIdentifiable, ResultCallbackPayload.create(Event.Event1){
            updatedStr = it
            true
        })
        val result = callbackManagerWithReturn.trigger(thisIdentifiable, Event.Event1, newValue)
        assertEquals(newValue, updatedStr)
        val receivedResult =  assertNotNull(result)
        assertTrue(receivedResult)
    }

    @Test
    fun `Callback transfer algorithms work as expected`() {
        var updatedStr1 = ""
        var updatedStr2 = ""
        var updatedStr2_4 = ""

        val newValue = "New Value"
        callbackManager.clear()
        callbackManager2.clear()

        callbackManager.subscribe(thisIdentifiable, CallbackPayload.create(Event.Event1) { updatedStr1 = it })
        callbackManager.subscribe(thisIdentifiable, CallbackPayload.create(Event.Event2) { updatedStr2 = it })

        callbackManager2.subscribe(thisIdentifiable, CallbackPayload.create(Event.Event4) { updatedStr2_4 = it })

        var mnr1TriggerCount = 0
        callbackManager.onAfterTriggered = { postTrigger -> mnr1TriggerCount = postTrigger.triggeredCount }

        var mnr2SubscriptCount: Int = 0
        callbackManager2.onNewSubscription = { state -> mnr2SubscriptCount = state.registrySize }

        var mnr2TrigCount = 0
        callbackManager2.onAfterTriggered = { postTrigger -> mnr2TrigCount += postTrigger.triggeredCount }

        callbackManager.transferTo(callbackManager2, listOf(Event.Event1, Event.Event2)) { input ->
            CallbackPayload.create(input.event, input.callback)
        }

        callbackManager2.triggerForAll(Event.Event1, "${newValue}_callbackManager2")
        callbackManager2.triggerForAll(Event.Event2, "${newValue}_callbackManager2")
        callbackManager2.triggerForAll(Event.Event3, "${newValue}_callbackManager2")
        callbackManager2.triggerForAll(Event.Event4, "${newValue}_callbackManager2")

        assertEquals(0, mnr1TriggerCount)
        assertEquals(3, mnr2SubscriptCount)
        assertEquals(3, mnr2TrigCount)

        assertAll(
            "Values updated appropriately",
            { assertEquals("${newValue}_callbackManager2", updatedStr1) },
            { assertEquals("${newValue}_callbackManager2", updatedStr2) },
            { assertEquals("${newValue}_callbackManager2", updatedStr2_4) }
        )
    }

    @Test
    fun `Callback transfer algorithms `() {

        val pay = ExperimentalPayload<String, ExpEvent>(ExpEvent.OnInit, CallableContainer())
        val payRes = ExperimentalResultPayload<String, Boolean, ExpEvent>(ExpEvent.OnComplete, CallableContainer())

        val experimental = ExperimentalCallbackManager(ExpEvent::class.java, pay, pay, payRes)
        val payload = experimental.getPayload<String, Boolean>(ExpEvent.OnComplete)


    }


}