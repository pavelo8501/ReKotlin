package po.test.misc.callback.validator

import org.junit.jupiter.api.Test
import po.misc.callbacks.validator.ReactiveValidator
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestValidator {

    private class ValidationData(
        var stringValue : String = "StringValue",
        var numericValue : Int = 300,
    )

    @Test
    fun `Validate function work as expected`(){

        val validator = ReactiveValidator<ValidationData>{ data->
            data.stringValue == "StringValue"
        }
        val data = ValidationData()
        assertTrue {
            validator.validate(data)
        }
        data.stringValue = "Should not"
        assertFalse {
            validator.validate(data)
        }
    }



}