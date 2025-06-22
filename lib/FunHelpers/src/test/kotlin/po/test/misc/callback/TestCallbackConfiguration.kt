package po.test.misc.callback

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.builders.callbackManager
import po.misc.callbacks.manager.models.Configuration
import po.misc.exceptions.ManagedException
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.interfaces.asIdentifiableClass

class TestCallbackConfiguration: IdentifiableClass {

    override val identity: ClassIdentity = asIdentifiableClass("TestUnit", "TestCallbackConfiguration")

    class FirstHoldingClass(private val callbackConfiguration: Configuration) : IdentifiableContext{
        enum class Event{ OnInit, OnRouted  }
        override val contextName: String = "FirstHoldingClass"

        val cbManager = callbackManager<Event>(config =  callbackConfiguration)
        val routedPayload = CallbackManager.createPayload<Event, String>(cbManager, Event.OnRouted)
    }


    @Test
    fun `Exception raised when nothing to trigger`(){

        val emitter = FirstHoldingClass(Configuration(exceptionOnTriggerFailure = true))
        val cbManager = emitter.cbManager

        assertThrows<ManagedException> {
            cbManager.trigger(FirstHoldingClass.Event.OnInit, 10)
        }
    }

}