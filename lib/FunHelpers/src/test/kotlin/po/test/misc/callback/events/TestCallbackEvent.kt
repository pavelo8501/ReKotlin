package po.test.misc.callback.events

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.EventValidator
import po.misc.callbacks.events.Validatable
import po.misc.callbacks.signal.signal
import po.misc.callbacks.signal.signalOf
import po.misc.data.helpers.output
import po.misc.exceptions.ManagedException
import po.misc.functions.NoResult
import po.misc.types.token.TypeToken
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
    fun `Callback event creation by type data`(){

       
    }

}