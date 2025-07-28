package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.console.DateHelper
import po.misc.data.printable.PrintableBase
import po.misc.data.styles.colorize
import po.misc.data.processors.DataProcessor
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.text
import po.misc.data.printable.companion.PartsTemplate
import po.misc.interfaces.ValueBased
import kotlin.test.assertTrue
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.data.printable.PrintableGroup
import po.misc.data.printable.companion.nextLine
import kotlin.test.assertNotEquals


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
    ): PrintableBase<Item>(this){

        override val self: Item = this

      //  override val itemId: ValueBased = toValueBased(0)

        companion object: PrintableCompanion<Item>({Item::class}){

            val Header = createTemplate(){
                nextLine {
                    "$personalName | $componentName | $description".colorize(Colour.BLUE)
                }
            }

            val Footer = createTemplate(){
                nextLine {
                    "$personalName Value = ${intValue}".colorize(Colour.BLUE)
                }
            }

            val Printable: PartsTemplate<Item> = PartsTemplate(
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
    ): PrintableBase<Item2>(this){

        override val self: Item2 = this
       // override val itemId: ValueBased = toValueBased(0)

        companion object: PrintableCompanion<Item2>({Item2::class}){

            val RegularStr = createTemplate{
                nextLine {
                    "$componentName in $module".colorize(Colour.CYAN)
                }
            }

            val Template2 = createTemplate{
                nextLine {
                    "String2-> $personalName | $module And Value=$handled".colorize(Colour.RED)
                }
            }
        }
    }

    class ItemGroup(
        groupHost:Item,
    ) : PrintableGroup<Item, Item2>(groupHost, Item.Header, Item2.RegularStr)

    @Test
    fun `Test printable group`(){

        val item = Item("groupHost")
        val group1 = ItemGroup(item)
        group1.setHeader(Item.Header)
        group1.setFooter(Item.Footer)

        for(i in 1..11){
            val childItem = Item2("childItem_${i}", componentName = "Component#${i}")
            group1.addRecord(childItem, )
        }
        val string = group1.formattedString
        assertTrue(string.contains("Component#3"))
        println(string)

    }

    @Test
    fun `Printable metadata properly initialized`(){
        val item = Item("name")
        val item2 = Item2("name")
        assertTrue(Item.metaDataInitialized)
        assertTrue(Item2.metaDataInitialized)
        assertNotEquals(Item.typeKey.hashCode(), Item2.typeKey.hashCode(), "Keys are the same")
    }

}
