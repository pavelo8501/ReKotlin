package po.test.misc.callback

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.builders.callbackManager
import po.misc.callbacks.models.Configuration
import po.misc.context.CTX
import po.misc.context.asIdentity
import po.misc.exceptions.ManagedException


class TestCallbackConfiguration:  CTX {


    override val identity = asIdentity()

    internal class FirstHoldingClass(private val callbackConfiguration: Configuration) : CTX{
        enum class Event{ OnInit, OnRouted  }

        override val identity = asIdentity()

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