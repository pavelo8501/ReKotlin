package po.test.misc.reflection.annotations

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.reflection.anotations.BuilderProperty
import po.misc.reflection.anotations.annotatedProperties
import po.misc.reflection.builders.ConstructableClass
import po.test.misc.reflection.builders.TestConstructable.ForTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestAnnotationHelpers {

    data class TestableClass(

        @BuilderProperty
        val property1: String = "nope",

        val intProperty: Int = 10,

        @BuilderProperty
        val booleanProperty: Boolean = true,

        @BuilderProperty
        var varProperty1: String = "default",
        @BuilderProperty
        var varIntProperty: Int = 10,
        @BuilderProperty
        var varBooleanProperty: Boolean = true,
        @BuilderProperty
        var varEnum: ForTest = ForTest.Nope

    ) {

        companion object : ConstructableClass<TestableClass>() {
            override val builder: () -> TestableClass get() = { TestableClass() }
            override fun build(): TestableClass {
                return TestableClass()
            }
        }
    }

    @Test
    fun `Values are fully read`(){

        val valuesRead = mutableListOf<Any>()

        val source = TestableClass.build()
        val container = annotatedProperties<TestableClass, BuilderProperty>(source)
        val pairs = container.propertyPairs
        assertEquals(6, pairs.size)
        pairs.forEach {
           val value = assertNotNull(it.value)
           valuesRead.add(value)
        }
        valuesRead.output()
    }

}