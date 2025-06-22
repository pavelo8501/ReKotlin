package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.smartLazy
import kotlin.test.assertEquals

class TestValueHelpers {

    var nullableStr: String? = null
    val defaultValue = "Default value"

    val result : String by smartLazy(
        lazyProvider =  {
            nullableStr
          },
        default = defaultValue
    )


    @Test
    fun `Lazy works as expected`(){

        assertEquals(defaultValue, result)
        val valueChanged = "ValueChanged"
        nullableStr = valueChanged

        assertEquals(valueChanged, result)
        assertEquals(valueChanged, result)
    }

}