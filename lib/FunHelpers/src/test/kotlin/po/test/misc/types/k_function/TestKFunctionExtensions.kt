package po.test.misc.types.k_function

import org.junit.jupiter.api.Test
import po.misc.types.k_function.receiverClass
import kotlin.test.assertNotNull

class TestKFunctionExtensions {

    fun someMethod(){}

    @Test
    fun `Getting class from KFunction`(){
        assertNotNull(::someMethod.receiverClass())
        assertNotNull(TestKFunctionExtensions::someMethod.receiverClass())
    }

}