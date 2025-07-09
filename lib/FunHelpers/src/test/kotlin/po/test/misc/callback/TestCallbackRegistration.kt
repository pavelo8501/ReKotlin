package po.test.misc.callback

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.bridgeFrom
import po.misc.callbacks.builders.callbackBuilder
import po.misc.callbacks.builders.createPayload
import po.misc.callbacks.builders.createPayloadWithResult
import po.misc.exceptions.ManagedException
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestCallbackRegistration() : IdentifiableClass {

    override val identity = asIdentifiableClass("TestCallbackDataHandling", "Test")
    enum class HostEvent{ Unused, UnusedBuilder, UnusedSelfSubscribed }

    class FirstHoldingClass: IdentifiableContext{
        enum class Event{ OnInit }
        override val contextName: String = "HoldingClass"

        val notifier = CallbackManager(
            enumClass = Event::class.java,
            emitter = this
        )
        val onInitEventPayload= CallbackManager.createPayload<Event, Int>(notifier, Event.OnInit)
    }

    @Test
    fun `Creating multiple payloads of the same type results in error`(){
        assertThrows<ManagedException> {
          val notifier =  CallbackManager(
                enumClass = HostEvent::class.java,
                emitter = this
            )
            CallbackManager.createPayload<HostEvent, String>(notifier, HostEvent.UnusedBuilder)
            CallbackManager.createPayload<HostEvent, String>(notifier, HostEvent.UnusedBuilder)
        }
    }

    @Test
    fun `Creating multiple same key payloads of different type`(){
       val manager = assertDoesNotThrow {
           val notifier =  CallbackManager(
                enumClass = HostEvent::class.java,
                emitter = this
            )
           CallbackManager.createPayload<HostEvent, String>(notifier, HostEvent.UnusedBuilder)
           CallbackManager.createPayload<HostEvent, Int>(notifier, HostEvent.UnusedBuilder)
           notifier
        }
        val managerInfo = manager.getStats()
        assertEquals(1, managerInfo.eventTypesCount, "Registered event count should be 1")
        assertEquals(2, managerInfo.payloadsCount, "Payloads count should be 2")
    }

    @Test
    fun `Creating multiple different key payloads of same type`(){
        val manager = assertDoesNotThrow {
            val notifier =  CallbackManager(
                enumClass = HostEvent::class.java,
                emitter = this
            )
            CallbackManager.createPayload<HostEvent, String>(notifier, HostEvent.UnusedBuilder)
            CallbackManager.createPayload<HostEvent, String>(notifier, HostEvent.UnusedSelfSubscribed)
            notifier
        }
        val managerInfo = manager.getStats()
        assertEquals(2, managerInfo.eventTypesCount, "Registered event count should be 2")
        assertEquals(2, managerInfo.payloadsCount, "Payloads count should be 2")
    }

    @Test
    fun `DSL style builder creates manager as expected`(){

        val holder1 = FirstHoldingClass()
        val manager1 = FirstHoldingClass().notifier


        holder1.onInitEventPayload.subscribe(this){

        }

        val manager = callbackBuilder<HostEvent> {
            createPayload<HostEvent, Int>(HostEvent.UnusedBuilder){
                bridgeFrom(holder1.onInitEventPayload)
            }
            createPayload<HostEvent, Int>(HostEvent.UnusedSelfSubscribed)
            createPayloadWithResult<HostEvent, String, Int>(HostEvent.Unused)
        }
        val managerInfo = manager.getStats()
        assertEquals(2, managerInfo.eventTypesCount, "Registered event count should be 2")
        assertEquals(3, managerInfo.payloadsCount, "Payloads count should be 3")
        assertEquals(1, managerInfo.routedContainersCount, "Bridge connection not created")
    }

    fun `Errors thrown when faulty setup provided`(){
//        val manager = FirstHoldingClass().notifier
//        val exception = assertThrows<ManagedException> {
//            manager.trigger(FirstHoldingClass.Event.OnInit, 10)
//        }
//        val managed = assertNotNull(exception)
//        assertTrue(managed.message.contains("trigger"))
    }


    fun `Nothing is thrown if exception handler provided`(){
        val notifier1 = FirstHoldingClass().notifier
        var caught: ManagedException? = null
        notifier1.setExceptionHandler {
            caught = it
        }
        assertDoesNotThrow {
            notifier1.trigger(FirstHoldingClass.Event.OnInit, 10)
        }
        assertNotNull(caught, "Exception should have been sent by callback")
    }

}