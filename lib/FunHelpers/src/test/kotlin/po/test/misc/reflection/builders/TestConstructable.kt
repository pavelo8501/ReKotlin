package po.test.misc.reflection.builders

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import po.misc.reflection.anotations.BuilderProperty
import po.misc.reflection.anotations.annotatedProperties
import po.misc.reflection.builders.Constructable
import po.misc.reflection.builders.ConstructableClass
import po.misc.reflection.properties.findMutableOfType
import po.misc.reflection.properties.findPropertiesOfType
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestConstructable {


    enum class ForTest {
        Yep,
        Nope
    }

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


    class BuilderReceiver<T : Any>(
        val constructable: Constructable<T>
    ) {

        fun construct(): T {
            return constructable.builder()
        }

    }

    @Test
    fun `Constructable usage`() {
        val builtObject = assertDoesNotThrow {
            TestableClass.build()
        }
        val builtObjectByProperty = TestableClass.builder()
        assertEquals(builtObject, builtObjectByProperty)

    }

    @Test
    fun `Constructable usage with other objects`() {
        val receiver = BuilderReceiver<TestableClass>(TestableClass)
        assertDoesNotThrow {
            receiver.construct()
        }
    }

    @Test
    fun `Test annotation container instantiation`() {

        val testable = TestableClass()
        val container = TestableClass.buildPropertyContainer<BuilderProperty>()
        val propertyContainer = assertNotNull(container)
        assertEquals(6, propertyContainer.propertyPairs.size)
        val secondCall = assertNotNull(container)
        assertSame(propertyContainer, secondCall)
    }

    @Test
    fun `Test property updates `() {

        val valueInput = "newValue"
        val testable = TestableClass()
        val container = TestableClass.buildPropertyContainer<BuilderProperty>()
        val updatePair = Pair("property1", valueInput)
        val result = assertDoesNotThrow {
            container.updateConverting(testable, updatePair)
        }
        assertFalse(result)

        val intByString = "200"
        val newBoolean = false
        val updateList = listOf(
            Pair("varProperty1", valueInput),
            Pair("varIntProperty", intByString),
            Pair("varBooleanProperty", newBoolean)
        )
        val success = assertDoesNotThrow {
            container.updateConverting(testable, updateList)
        }
        assertTrue(success)

        val enum =  Pair("varEnum", ForTest.Yep)
        container.updateConverting(testable, enum)
        assertEquals(ForTest.Yep, testable.varEnum)

        val enum2 =  Pair("varEnum", "Nope")
        container.updateConverting(testable, enum2)
        assertEquals(ForTest.Nope, testable.varEnum)

    }
    @Test
    fun `Test property lookups `() {

        val testable = TestableClass()

        val count =  testable.findPropertiesOfType(String::class)
        assertEquals(2, count.size)

        val count2 = findPropertiesOfType<TestableClass, String>()
        assertEquals(2, count2.size)

        val count3 = findMutableOfType<TestableClass, String>()
        assertEquals(1, count3.size)

        val count4 =   testable.findMutableOfType(String::class)
        assertEquals(1, count4.size)
    }

}