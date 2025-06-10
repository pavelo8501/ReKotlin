package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.reflection.mappers.PropertyMapper

class TestPropertyMapper {

    enum class ID(override val value : Int): ValueBased{
        CLASS_1(1)
    }

    data class SourceClass(
        val property1: String = "Property1",
        val property2 : Int = 10,
        val property3: Boolean = false, override val sourceName: String = ""
    ): Identifiable{
        override val componentName: String = "SourceClass1"
    }

    @Test
    fun `Property map`(){

        val sourceClass = SourceClass()
        val propertyMap = PropertyMapper()
        propertyMap.applyClass<ID>(ID.CLASS_1)
        propertyMap.mappedProperties
        val a = propertyMap.mappedProperties
    }

}