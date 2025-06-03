package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.console.Colour
import po.misc.data.console.PrintHelper
import po.misc.data.console.PrintableBase
import po.misc.data.console.PrintableTemplate
import po.misc.interfaces.ValueBasedClass
import po.test.misc.data.TestPrintableBase.Item.Companion.Footer
import po.test.misc.data.TestPrintableBase.Item.Companion.Header
import po.test.misc.data.TestPrintableBase.Item.Companion.Infix
import po.test.misc.data.TestPrintableBase.Item.Companion.External

class TestPrintableBase: PrintHelper {

    data class Item (
        val name: String = "Some name",
        val description : String = "description",
        val intValue: Int = 200
    ): PrintableBase<Item>(){
        override val self: Item = this

        companion object{
            object Header : ValueBasedClass(1)
            object Footer : ValueBasedClass(2)
            object Infix :  ValueBasedClass(3)
            object External :  ValueBasedClass(4)

            val Printable: PrintableTemplate<Item> = PrintableTemplate(5){
                "Printable->  $name | $description And Value=$intValue"
            }
        }
    }

    @Test
    fun `Printable base`(){
        var value3 = 9

       val item1 : Item = Item()
        item1.setTemplate(Header) { "Header-> $name | $description ${makeOfColour("And Value=$intValue", Colour.BRIGHT_BLUE)}"}
        item1.setTemplate(Footer) { "Footer->  $name | $description And Value=$intValue" }
        item1.setTemplate(Infix) { "Some text to be printed magenta" colourOf Colour.MAGENTA }

        val output = "Danger level".makeOfColour(param =  item1.intValue,
            colourRule<Int>(Colour.GREEN){ it > 100 },
            colourRule<Int>(Colour.RED){ it > 10 },
            colourRule<Int>(Colour.YELLOW){ it > 5 },
        )
        println(output)

        val output2 = "Danger level22222".makeOfColour(
            colourRule(Colour.RED){ item1.intValue > 100 },
            colourRule(Colour.BRIGHT_MAGENTA){ item1.intValue > 10 },
            colourRule(Colour.YELLOW){ item1.intValue > 5 }
        )
        println(output2)

        item1.print("Some message")
        item1.print(Header)
        item1.print(Footer)
        item1.print(Infix)

        item1.setTemplate(External){
            "Danger level22222".makeOfColour(
                colourRule(Colour.RED){ value3 < 10 },
                colourRule(Colour.BRIGHT_MAGENTA){ value3 <= 21 },
                colourRule( Colour.YELLOW){ value3 > 21 },
            )
        }
        item1.print(External)

        item1.print(Item.Printable)
    }

}