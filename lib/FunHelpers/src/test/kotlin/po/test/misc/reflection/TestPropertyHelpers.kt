package po.test.misc.reflection

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import po.misc.reflection.mappers.models.PropertyContainer
import po.misc.reflection.properties.toPropertyMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.test.assertEquals

class TestPropertyHelpers {

    class CheckedPropertyRecord(
        val propertyRecord : PropertyContainer<*>,
        var checked: Boolean = false
    ): PropertyContainer<Any>{

        override val propertyName: String
            get() = propertyRecord.propertyName
        override val property: KProperty<Any>
            get() = propertyRecord.property
    }

    class TestDelegate<T>( var activeValue :T) : ReadWriteProperty<T, T>{
        override fun getValue(thisRef: T, property: KProperty<*>): T {
           return activeValue
        }
        override fun setValue(thisRef: T, property: KProperty<*>, value: T) {
            activeValue = value
        }
    }

    class SourceClass(
        val property1: String = "Property1",
        val property2 : Int = 10,
        val property3: Boolean = false
    ){
        val delegate1: TestDelegate<String> = TestDelegate("Delegate value")
        val delegate2: TestDelegate<Int> = TestDelegate(10)
        val delegate3: TestDelegate<Int> = TestDelegate(20)
    }

    @Test
    fun `Property info fully initialized`(){
        val instance = SourceClass()
    }


    @Test
    fun `Selection contains only delegate properties`(){

        val delegatePropertiesWildcard = toPropertyMap<SourceClass>()
        val delegatePropertiesStr = toPropertyMap<SourceClass>()
        val delegatePropertiesInt = toPropertyMap<SourceClass>()

        assertEquals(3, delegatePropertiesWildcard.size)
        assertEquals(1, delegatePropertiesStr.size)
        assertEquals(2, delegatePropertiesInt.size)

        val transformedMap : Map<String, PropertyContainer<*>> =
            delegatePropertiesStr.mapValues{(_, v)-> CheckedPropertyRecord(v) } +
            delegatePropertiesInt.mapValues{(_, v)-> CheckedPropertyRecord(v) }

        assertEquals(3, transformedMap.size)
        assertInstanceOf<CheckedPropertyRecord>(transformedMap.values.firstOrNull())
    }

}