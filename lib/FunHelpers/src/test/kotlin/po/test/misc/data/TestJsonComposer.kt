package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import po.misc.data.json.models.JsonObject
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.types.TypeData

class TestJsonComposer {

    internal data class NestedData(
        val otherName: String = "SomeName"
    )


    internal class Data(
        val name: String,
        val value: Int,
        val nested: List<NestedData> = listOf(NestedData(), NestedData("SomeName2"))
    ) : PrintableBase<Data>(this) {

        override val self = this

        companion object : PrintableCompanion<Data>({ Data::class }) {

        }
    }

    @Test
    fun `Json dsl composer creates appropriate structure`() {

        val data = Data("name_property", 1)
        val jsonObject = JsonObject(TypeData.create<Data>())

        jsonObject.createRecord(Data::name)
        jsonObject.createRecord(Data::value)

        jsonObject.createList(Data::nested,  NestedData::otherName)

        val jsonOutput = jsonObject.toJson(data)
        jsonOutput.output()

    }

}