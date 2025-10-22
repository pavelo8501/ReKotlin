package po.test.misc.callback.event

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.callbacks.common.EventHost
import po.misc.callbacks.event.event
import po.misc.exceptions.handling.Suspended
import po.misc.functions.NoResult
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestValidator: EventHost {

    private class ClassToBeValidated(val parameter: String = "Param1")

    @Test
    fun `InvokeValidator usage with  HostedEvent`(){
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
               it.parameter == "Param1"
            }
        }
        assertNotNull(validatableEvent.validator)
    }

    @Test
    fun `InvokeValidator on success`(){
        var success: Boolean = false
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                it.parameter == "Param1"
            }.onSuccess {
                success = true
            }
        }

        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid)

        assertTrue { success }
    }

    @Test
    fun `InvokeValidator on failure`(){

        val success: Any? = null
        var failure: Any? = null
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                it.parameter == "Param"
            }.onFailure {
                failure = it
            }
        }
        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid)
        assertNull(success)
        assertIs<ClassToBeValidated>(failure)
    }


    @Test
    fun `InvokeValidator on success suspending`()= runTest{

        var success: Boolean? = null
        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                it.parameter == "Param1"
            }.onSuccess(Suspended){
                success = true
            }
        }
        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid, Suspended)
        assertTrue { success?:false }
    }


    @Test
    fun `InvokeValidator on failure suspending`()= runTest{

        val success: Any? = null
        var failure: Any? = null

        val validatableEvent = event<TestValidator, ClassToBeValidated>(NoResult) {
            withValidation {
                it.parameter == "Param"
            }.onFailure(Suspended) {
                failure = it
            }
        }
        assertNotNull(validatableEvent.validator)
        val valid = ClassToBeValidated()
        validatableEvent.triggerValidating(valid, Suspended)
        assertNull(success)
        assertIs<ClassToBeValidated>(failure)
    }

}