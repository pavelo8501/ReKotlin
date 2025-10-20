package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.json.models.JsonObject
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.types.token.TypeToken
import po.misc.types.type_data.TypeData

class TestJsonComposer {

    internal data class NestedData1(
        val nested1Name: String = "nested1NameString"
    )

    internal data class NestedData2(
        val nested2Name: String = "nested2NameString"
    )


    internal class Data(
        val name: String,
        val value: Int,
        val nested: List<NestedData1> = listOf(NestedData1(), NestedData1("SomeName2")),
        val nested2: List<NestedData2> = listOf(NestedData2())
    ) : PrintableBase<Data>(this) {

        override val self = this

        companion object : PrintableCompanion<Data>(TypeToken.create()) {

        }
    }

    @Test
    fun `JsonObject creates appropriate formatting`() {

        val data = Data("name_property", 1)
        val jsonObject = JsonObject<Data, Data>(TypeToken.create<Data>())

        jsonObject.createRecord(Data::name)
        jsonObject.createRecord(Data::value)
        jsonObject.createObject(Data::nested,   NestedData1::nested1Name)
        jsonObject.createObject(Data::nested2,  NestedData2::nested2Name)

        val jsonOutput = jsonObject.toJson(data)
        jsonOutput.output()
    }

}