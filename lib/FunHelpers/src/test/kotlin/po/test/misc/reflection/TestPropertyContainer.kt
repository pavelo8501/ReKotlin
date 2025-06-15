package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.reflection.properties.PropertyAccess
import po.misc.reflection.properties.assignIfMatches
import po.misc.reflection.properties.createContainer
import po.misc.reflection.properties.typeTokenOf
import po.test.misc.reflection.TestPropertyHelpers.TestDelegate
import kotlin.test.assertEquals

class TestPropertyContainer {

    class SourceClass(
        var property1: String = "Property1",
        var property2 : Int = 10,
        var property3: Boolean = false
    ){
        var delegate1: TestDelegate<String> = TestDelegate("Delegate value")
        var delegate2: TestDelegate<Int> = TestDelegate(10)
        var delegate3: TestDelegate<Int> = TestDelegate(20)
    }

    @Test
    fun `Property container store data`(){

       val instance = SourceClass()

       val container = instance.createContainer("Test Container")
        typeTokenOf(instance.property1).assignIfMatches(container.propertyMap)
        assertEquals(6, container.propertyMap.size)

        instance.property1 = "New next"
        instance.property2 = 30

        container.updateData(instance)
        val updated = container.getPropertiesChanged()
        assertEquals(2, updated.size)

        val a = 10

    }
}