package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.types.TaggedType
import po.misc.types.TypeData
import po.test.misc.reflection.TestPropertyHelpers
import kotlin.test.assertNotEquals

class TestTypeData {

   internal data class SourceClass1(val str: String = "Default", val longVal: Long = 10L)
   internal data class SourceClass2(val str: String = "Default", val longVal: Long = 10L)

    enum class SourceClassType{
        Source1,
        Source2
    }

    internal class ThreeGenericsClass<T1: Any, T2: Any, T3: Any>()

    @Test
    fun `Type record usage on multi generic classes`(){
        val typeData = TypeData.create<ThreeGenericsClass<SourceClass1, SourceClass2, TestPropertyHelpers.SourceClass>>()
        println(typeData.typeName)
    }


    @Test
    fun `Type records can be reliably compared`(){

        val sourceClass1 = SourceClass1()
        val sourceClass2 = SourceClass1("Default2", 100)

        val typeData = TypeData.create<SourceClass1>()
        val typeData2 = TypeData.create<SourceClass2>()
        val typeData3 = TypeData.create<List<SourceClass2?>>()

        assertNotEquals<Any>(typeData, typeData2, "Equal but should not")
        assertNotEquals<Any>(typeData, typeData3, "Equal but should not")
    }

    @Test
    fun `TaggedType records can be reliably compared`(){

      val tagged1 :TaggedType<SourceClass2, SourceClassType> = TaggedType.create<SourceClass2, SourceClassType>(SourceClassType.Source1)
      println(tagged1)

    }

}