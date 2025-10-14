package po.test.misc.reflection.annotations

import org.junit.jupiter.api.Test
import po.misc.reflection.anotations.collectAnnotated
import po.misc.reflection.primitives.BooleanClass
import po.misc.reflection.primitives.IntClass
import po.misc.reflection.primitives.LongClass
import po.misc.reflection.properties.typed_property.toTypedProperties
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class TestAnnotatedProperty {


    @InTestAnnotation(4)
    var anotherProperty: String = "anotherProperty"

    @InTestAnnotation(2)
    var property1: Int = 777

    @InTestAnnotation(1)
    var property2: Long = 0L

    var booleanProperty: Boolean = false

    @Test
    fun `Test builder function`(){
        val containers = collectAnnotated<TestAnnotatedProperty, InTestAnnotation>()
        val sorted =  containers.sortedBy { it.annotation.order }
        assertEquals(3, containers.size)
        assertEquals("property2", sorted[0].property.name)
        assertEquals("anotherProperty", sorted[2].property.name)
    }

    @Test
    fun `Test annotated container`() {
        val containers = collectAnnotated<TestAnnotatedProperty, InTestAnnotation>()
        val mutable = containers.toTypedProperties()
        assertEquals(3, mutable.size)
        val properties = containers.map { it.property }.toTypedProperties()
        assertEquals(3, properties.size)
    }

    @Test
    fun `Reading Writing Values`() {
        val properties  = collectAnnotated<TestAnnotatedProperty, InTestAnnotation>().toTypedProperties()
        val propertyInt = assertNotNull(properties.firstOrNull{ it.primitiveClass == IntClass })
        propertyInt.updateValue(this, "100500")
        assertEquals(100500, this.property1)

        val propertyLong = assertNotNull(properties.firstOrNull{ it.primitiveClass == LongClass })
        propertyLong.updateValue(this, "300")
        assertEquals(300, this.property2)
    }

    @Test
    fun `Boolean Writing Values`() {
        val properties = TestAnnotatedProperty::class.toTypedProperties<TestAnnotatedProperty>()

        val boolean = assertNotNull(properties.firstOrNull { it.primitiveClass == BooleanClass })

        boolean.updateValue(this, "1")
        assertEquals(true,  booleanProperty)


    }


}