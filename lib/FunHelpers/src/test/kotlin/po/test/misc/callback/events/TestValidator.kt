package po.test.misc.callback.events

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.callbacks.events.EventHost
import po.misc.callbacks.events.event
import po.misc.exceptions.handling.Suspended
import po.misc.functions.NoResult
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestValidator: EventHost {


    internal class ClassToBeValidated(val parameter: String = "Param1")

    @Test
    fun `InvokeValidator usage with  HostedEvent`(){
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                registerValidator {
                    it.parameter == "Param1"
                }
            }
        }
        assertNotNull(validatableEvent.validator)
    }

    @Test
    fun `InvokeValidator on success`(){
        var success: Boolean? = null
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                registerValidator {
                    it.parameter == "Param1"
                }
            }.onValidationSuccess {
                success = true
            }
        }
        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid)
        assertTrue { success?:false }
    }

    @Test
    fun `InvokeValidator on success suspending`()= runTest{

        var success: Boolean? = null
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                registerValidator {
                    it.parameter == "Param1"
                }
            }.onValidationSuccess(Suspended) {
                success = true
            }
        }
        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid, Suspended)
        assertTrue { success?:false }
    }

    @Test
    fun `InvokeValidator on failure`(){

        var success: Any? = null
        var failure: Any? = null
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                registerValidator {
                    it.parameter == "Param"
                }
              onValidationFailure {
                  failure = it
                }
            }.onValidationSuccess {
                success = true
            }
        }
        assertNotNull(validatableEvent.validator)

        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid)
        assertNull(success)
        assertIs<ClassToBeValidated>(failure)
    }

    @Test
    fun `InvokeValidator on failure suspending`()= runTest{

        var success: Any? = null
        var failure: Any? = null

        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                registerValidator {
                    it.parameter == "Param"
                }
                onValidationFailure(Suspended) {
                    failure = it
                }

            }.onValidationSuccess(Suspended) {
                success = it
            }
        }
        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid, Suspended)
        assertNull(success)
        assertIs<ClassToBeValidated>(failure)
    }


}