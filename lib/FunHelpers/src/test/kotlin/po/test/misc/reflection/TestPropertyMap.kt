package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.collections.CompositeKey
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.properties.PropertyMap

class TestPropertyMap {

    enum class ID(override val value : Int): ValueBased{
        CLASS_1(1)
    }

    data class SourceClass(
        val property1: String = "Property1",
        val property2 : Int = 10,
        val property3: Boolean = false
    ): Identifiable{
        override val qualifiedName: String = "sourceClass"
    }

    @Test
    fun `Property map`(){

        val sourceClass = SourceClass()
        val propertyMap = PropertyMap()
        propertyMap.applyClass(ID.CLASS_1, SourceClass::class)
        propertyMap.mappedProperties
        val a = propertyMap.mappedProperties
    }

}