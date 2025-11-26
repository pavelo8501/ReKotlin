package po.test.misc.data.logging.parts

import org.junit.jupiter.api.Test
import po.misc.data.output.output
import po.misc.data.logging.parts.ReflectiveTable
import po.misc.data.logging.parts.ValueSnapshot
import po.misc.data.logging.parts.reflectiveTable
import kotlin.test.assertEquals

class TestNameValue {

    @ValueSnapshot
    val otherProperty: Int = 300

    @ValueSnapshot
    val testProperty: String = "Otsosi u traktorista"

    @ValueSnapshot
    var  mutableOne: Boolean = false

    @Test
    fun `Name value pairs form a table`(){

        val reflective1 = reflectiveTable<TestNameValue> {
            addKey(::otherProperty)
            addKey(::testProperty)
            addKey(::mutableOne)
        }
        val reflective2 = ReflectiveTable(this)
        assertEquals(3, reflective1.pairsList.size)
        assertEquals(reflective1.pairsList.size, reflective2.pairsList.size)

        val reflective1Result = reflective1.applyKeysValues(this)
        val reflective2Result = reflective2.applyKeysValues(this)

        reflective1Result.output(" reflective1Result  ")

        reflective2Result.output( " reflective2Result  " )

    }

}