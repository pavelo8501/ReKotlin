package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.collections.StaticTypeKey
import po.misc.data.console.DateHelper
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.styles.colorize
import po.misc.data.processors.DataProcessor
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.text
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.interfaces.toValueBased
import kotlin.test.assertTrue
import po.misc.data.printable.PrintableCompanion
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame


class TestPrintableBase: DateHelper {

    enum class Events(override val value: Int) : ValueBased{
        Info(1),
        Warn(2)
    }

    /***
     * DataProcessor should contain an information about data
     * composition rules.
     *
     */
    val processor: DataProcessor<Item> = DataProcessor<Item>(null,null)

    data class Item (
        val personalName: String,
        val componentName: String = "Some name",
        val description : String = "description",
        val intValue: Int = 200
    ): PrintableBase<Item>(Printable){

        override val self: Item = this

        override val emitter: Identifiable = asIdentifiable(personalName, componentName)
      //  override val itemId: ValueBased = toValueBased(0)

        companion object: PrintableCompanion<Item>({Item::class}){

            val Printable: PrintableTemplate<Item> = PrintableTemplate(
                templateName = "Printable",
                delimiter = SpecialChars.NewLine.char,
                { Colour.RED text "String1->  $personalName |"},
                {"$description And Value=$intValue"}
            )
        }
    }

    /***
     * Item2 can come from any module. Item and Item2 shall not know anything
     * about each other except for shared base.
     */
    data class Item2 (
        val personalName: String,
        val componentName: String = "Some name",
        val error: String = "Generic error",
        val module : String = "Module1",
        val handled: Boolean = true,
    ): PrintableBase<Item2>(Template2){

        override val self: Item2 = this
        override val emitter: Identifiable = asIdentifiable(personalName, componentName)
       // override val itemId: ValueBased = toValueBased(0)

        companion object: PrintableCompanion<Item2>({Item2::class}){
            val Template2: PrintableTemplate<Item2> = PrintableTemplate<Item2>("Template2"){
                "String2-> $personalName | $module And Value=$handled".colorize(Colour.RED)
            }
        }
    }

    @Test
    fun `Printable metadata properly initialized`(){
        val item = Item("name")
        val item2 = Item2("name")
        assertTrue(Item.metaDataInitialized)
        assertTrue(Item2.metaDataInitialized)
        assertNotEquals(Item.typeKey.hashCode(), Item2.typeKey.hashCode(), "Keys are the same")
    }



//
//    @Test
//    fun `Test processor creates data`(){
//         var created : PrintableBase<*>? = null
//         processor.registerEvent<Item>(Events.Info){
//             created = it
//
//        }
//        info("Some message")
//        assertNotNull(created, "Record to be created is null")
//    }
//
//    @Test
//    fun `Data processor able to apply mute conditions dynamically`(){
//
//        processor.provideMuteCondition {
//            it.itemId.value == 1
//        }
//
//        processor.provideMuteCondition<Item2> {
//            it.error == "NewError"
//        }
//
//        val item1 = Item(personalName = "Item 1", description = "Description3")
//        val item2 = Item2(personalName = "Item 2", error = "NewError")
//
//        val message1 = processor.processRecord(item1, null)
//        val message2 = processor.processRecord(item2, null)
//        assertNull(message1)
//        assertNull(message2)
//    }
//
//    @Test
//    fun `Assert Data processor able to obtain templated data from records`(){
//        val item1 = Item(personalName =  "Item 1", description = "Description3")
//        val item2 = Item2(personalName = "Item 2")
//
//        val message1 = processor.processRecord(item1, Item.Printable)
//        val message2 = processor.processRecord(item2, Item2.Template2)
//
//        assertEquals(2, processor.recordsCount, "Records not saved")
//        assertTrue(message1?.contains("String1->")?:false, "Output in message1 is different. Actual: $message1")
//        assertTrue(message2?.contains("String2->")?:false, "Output in message2 is different")
//    }
//
//    @Test
//    fun `Composition for instances`(){
//        val item =  Item(personalName = "Item 1", description = "Description3")
//        val item2List  : MutableList<Item2> = mutableListOf()
//
//        for(i in 1 .. 5){
//            item2List.add(Item2(personalName =  "Item2 $i"))
//        }
//        item.addChildren(item2List.toList())
//        assertEquals(5, item.children.size, "Child count mismatch")
//        assertFalse(item.children.any { it.parentRecord == null })
//    }
//
//    @Test
//    fun `Data processor able to operate on  PrintableBase instances`(){
//        val item1 =  Item(personalName =  "Item 1", description = "Description3")
//        val item2 = Item2(personalName = "Item 2")
//
//        processor.processRecord(item1, null)
//        processor.processRecord(item2, null)
//        assertEquals(2, processor.recordsCount, "items cont mismatch")
//    }
}
