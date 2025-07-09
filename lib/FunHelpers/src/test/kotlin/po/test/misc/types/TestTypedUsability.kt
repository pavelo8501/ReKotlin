package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.data.tags.Tagged
import po.misc.types.TaggedType
import po.misc.types.TypeData
import kotlin.test.assertNotEquals

class TestTypedUsability {

    data class SourceClass1(val str: String = "Default", val longVal: Long = 10L)
    data class SourceClass2(val str: String = "Default", val longVal: Long = 10L)

    enum class SourceClassType{
        Source1,
        Source2
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