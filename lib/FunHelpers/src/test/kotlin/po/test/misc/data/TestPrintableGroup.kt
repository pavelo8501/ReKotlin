package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.companion.nextLine
import po.misc.data.printable.grouping.ArbitraryDataMap
import po.misc.types.token.TypeToken
import kotlin.test.assertEquals


class TestPrintableGroup {

    internal data class ItemType1(
        val personalName: String= "Some name",
        val intValue: Int = 200
    ) : PrintableBase<ItemType1>(this) {
        override val self: ItemType1 = this
        companion object : PrintableCompanion<ItemType1>(TypeToken.create()) {
            val Header = createTemplate { nextLine { "$personalName | $intValue" } }
        }
    }

    internal data class ItemType2(val personalName: String= "Some name") : PrintableBase<ItemType2>(this) {
        override val self: ItemType2 = this
        companion object : PrintableCompanion<ItemType2>(TypeToken.create()) {
            val Header = createTemplate { nextLine { personalName } }
        }
    }

    internal data class ItemType3(val intValue: Int = 200):PrintableBase<ItemType3>(this) {
        override val self: ItemType3 = this
        companion object : PrintableCompanion<ItemType3>(TypeToken.create()) {
            val Header = createTemplate { nextLine { intValue.toString() } }
        }
    }

//    @Test
//    fun `Test group`() {
//        val newItem = ItemType1()
//        val newItem2 = ItemType2()
//        val newItem3 = ItemType3()
//
//        val group = ArbitraryDataMap()
//        group.putPrintable(newItem)
//        group.putPrintable(newItem2)
//        group.putPrintable(newItem3)
//
//        assertEquals(3, group.keys.size)
//    }
//
//    @Test
//    fun `Test group postfix creation`() {
//
//        val expectedValue = "ItemType1[TestPrintableGroup]"
//
//        val newItem = ItemType1()
//        val group = ArbitraryDataMap()
//
//        group.putPrintable(newItem){ "[TestPrintableGroup]" }
//        assertEquals(expectedValue, group.keys.firstOrNull().toString())
//        val key = group.putPrintable(newItem){ "[Should not update]" }
//
//        assertEquals(expectedValue, group.keys.firstOrNull().toString())
//        assertEquals(2, group[key]?.size)
//    }
}