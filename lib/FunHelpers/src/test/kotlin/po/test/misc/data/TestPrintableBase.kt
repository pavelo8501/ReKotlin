package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.data.console.DateHelper
import po.misc.data.printable.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.data.styles.colorize
import po.misc.data.processors.DataProcessor
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.data.styles.text
import po.misc.context.Identifiable
import po.misc.context.asContext
import po.misc.interfaces.ValueBased
import kotlin.test.assertTrue
import po.misc.data.printable.PrintableCompanion
import kotlin.test.assertNotEquals


class TestPrintableBase: DateHelper, CTX {

    enum class Events(override val value: Int) : ValueBased{
        Info(1),
        Warn(2)
    }

    override val identity: CTXIdentity<out CTX> = asContext()

    /***
     * DataProcessor should contain an information about data
     * composition rules.
     *
     */
    val processor: DataProcessor<Item> = DataProcessor<Item>(null,null)

    data class Item (
        override val producer: CTX,
        val personalName: String,
        val componentName: String = "Some name",
        val description : String = "description",
        val intValue: Int = 200
    ): PrintableBase<Item>(Printable){

        override val self: Item = this

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
        override val producer : CTX,
        val personalName: String,
        val componentName: String = "Some name",
        val error: String = "Generic error",
        val module : String = "Module1",
        val handled: Boolean = true,
    ): PrintableBase<Item2>(Template2){

        override val self: Item2 = this
       // override val itemId: ValueBased = toValueBased(0)

        companion object: PrintableCompanion<Item2>({Item2::class}){
            val Template2: PrintableTemplate<Item2> = PrintableTemplate<Item2>("Template2"){
                "String2-> $personalName | $module And Value=$handled".colorize(Colour.RED)
            }
        }
    }

    @Test
    fun `Printable metadata properly initialized`(){
        val item = Item(this, "name")
        val item2 = Item2(this, "name")
        assertTrue(Item.metaDataInitialized)
        assertTrue(Item2.metaDataInitialized)
        assertNotEquals(Item.typeKey.hashCode(), Item2.typeKey.hashCode(), "Keys are the same")
    }

}
