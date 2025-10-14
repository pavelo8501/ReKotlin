package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.reflection.properties.createTypedProperties
import kotlin.test.assertEquals

class TestTypedContainer {

    class SomeClass(
        var string: String = "ss",
        var int: Int = 10
    )

    @Test
    fun `Typed container creation by class`(){

       val container = SomeClass::class.createTypedProperties()
        assertEquals(2, container.size)
    }
}