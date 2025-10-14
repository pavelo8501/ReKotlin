package po.test.misc.callback.events

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.EventValidator
import po.misc.callbacks.events.Validatable
import po.misc.callbacks.events.eventOf
import po.misc.data.helpers.output
import po.misc.exceptions.ManagedException
import po.misc.types.token.TypeToken
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestCallbackEvent: EventHost {


    class Data1

    class HostedData

    class ValidatableString(val value: String): Validatable
    class ValidatableLong(val value: Long): Validatable

    class StrValidator(
        validatorFn: (ValidatableString)-> Boolean
    ): EventValidator<ValidatableString>(TypeToken.create<ValidatableString>(), validatorFn)

    val testValue: String = "TestValue"

    @Test
    fun `Callback event`(){

        val click = eventOf<Data1>()
        var triggered: Any? = null

        click.onEvent  {
            triggered = it
        }

        val  data = Data1()

        click.trigger(data)
        assertIs<Data1>(triggered)


        triggered = null
        val presetEvent = eventOf<Data1> {
            onEvent {
                triggered = it
            }
        }

        presetEvent.trigger(data)
        assertIs<Data1>(triggered)
    }

    @Test
    fun `Callback event creation by type data`(){

       
    }

    @Test
    fun `Callback event with validation`(){

        var triggerResult: Any? = null

        val strValidator = StrValidator{
            it.value == "valid"
        }

        val  data = Data1()

        val click = eventOf<Data1>{
            onEvent {
                triggerResult = it
            }
        }.apply {
            addValidator(strValidator)
        }

        val falseValidatable = ValidatableString("Nope")
         click.trigger(data, falseValidatable)

        assertNull(triggerResult)

        val trueValidatable = ValidatableString("valid")
         click.trigger(data, trueValidatable)
        assertNotNull(triggerResult) 
        assertIs<Data1>(triggerResult)
    }

    @Test
    fun `Callback event with validations throwing`(){

        var triggerResult: Any? = null

        val  data = Data1()

        val strValidator = StrValidator{
            it.value == "valid"
        }
        val click = eventOf<Data1>{
            onEvent {
                triggerResult = it
            }
        }
        val falseValidatable = ValidatableString("Nope")
        var managed = assertThrows<ManagedException> {
            click.trigger(data, falseValidatable, validationThrows = true)
        }
        assertTrue { managed.message.contains("No Validators present") }
        managed.output()

        click.addValidator(strValidator)

        val validatableLong = ValidatableLong(300)
        managed = assertThrows<ManagedException> {
            click.trigger(data, validatableLong, validationThrows = true)
        }
        assertTrue { managed.message.contains("Wrong type") }
        managed.output()
    }

    @Test
    fun `Parametrized event`() {

        var triggered: Any? = null
        val hosted = HostedData()

        val event =  this.eventOf<TestCallbackEvent, HostedData> {
            onEvent {
                triggered = it
            }
        }
        event.trigger(hosted)
    }

    @Test
    fun `triggerBoth triggers both callbacks or synced`() = runTest {

        var triggered: Any? = null
        var triggeredBySuspended: Any? = null

        val event =  this@TestCallbackEvent.eventOf<TestCallbackEvent, HostedData> {
            onEvent {
                triggered = it
            }
            onEventSuspending {
                triggeredBySuspended = it
            }
        }
        val hosted = HostedData()
        event.triggerBoth(hosted, null)
        assertNotNull(triggered)
        assertNotNull(triggeredBySuspended)


        triggered = null
        triggeredBySuspended = null
        val event2 =  this@TestCallbackEvent.eventOf<TestCallbackEvent, HostedData> {
            onEvent {
                triggered = it
            }
        }
        event2.triggerBoth(hosted, null)
        assertNotNull(triggered)
        assertNull(triggeredBySuspended)

    }
}