package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.types.helpers.filterTokenized
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals

class TestFilterTokenized {

    private class SomeData(
        var stringValue: String = "SomeString",
        var intValue: Int = 10,
        override val typeToken: TypeToken<SomeData> = TypeToken.create()
    ): Tokenized<SomeData>


    private class OtherData(
        var stringValue: String = "SomeString",
        var intValue: Int = 300,
        override val typeToken: TypeToken<OtherData> = TypeToken.create()
    ): Tokenized<OtherData>


    @Test
    fun `Correct type not filtered out`(){
        val collection = listOf(
            OtherData(),
            OtherData("String2", 500)
        )

        val result = collection.filterTokenized<OtherData, OtherData>(OtherData::class)
        assertEquals(2, result.size)
    }

    @Test
    fun `Wrong type filtered out`(){

        val collection = listOf(
            OtherData(),
            OtherData("String2", 500)
        )
        val result = collection.filterTokenized<SomeData, SomeData>(SomeData::class)
        assertEquals(0, result.size)
    }

}