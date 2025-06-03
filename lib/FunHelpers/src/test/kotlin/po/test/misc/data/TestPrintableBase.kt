package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.console.Colour
import po.misc.data.console.PrintHelper
import po.misc.data.console.PrintableBase
import po.misc.interfaces.ValueBasedClass

class TestPrintableBase: PrintHelper {

    data class Item (
        val name: String = "Some name",
        val description : String = "description",
        val intValue: Int = 200
    ): PrintableBase<Item>(){
        override val self: Item = this

        init {
            setTemplate(Header) { "Header-> $name | $description ${makeOfColour("And Value=$intValue", Colour.BRIGHT_BLUE)}"}
            setTemplate(Footer) { "Footer->  $name | $description And Value=$intValue" }
            setTemplate(Infix) { "Some text to be printed magenta" colour Colour.MAGENTA }

            val output = "Danger level".makeOfColour(param =  intValue,
                rule({ it > 100 }, Colour.GREEN),
                rule({ it > 10 },  Colour.RED),
                rule({ it > 5 },   Colour.YELLOW),
            )
            println(output)


            val output2 = "Danger level22222".makeOfColour(
                staticRule({ intValue > 100 }, Colour.RED),
                staticRule({ intValue > 10 },  Colour.BRIGHT_MAGENTA),
                staticRule({ intValue > 5 },   Colour.YELLOW),
            )
            println(output2)
        }

        companion object{
            object Header : ValueBasedClass(1)
            object Footer : ValueBasedClass(2)
            object Infix :  ValueBasedClass(3)
            object External :  ValueBasedClass(4)
        }
    }

    @Test
    fun `Printable base`(){
        var value3 = 9

       val item1 : Item = Item()
        item1.print("Some message")
        item1.print(Item.Companion.Header)
        item1.print(Item.Companion.Footer)
        item1.print(Item.Companion.Infix)
        item1.setTemplate(Item.Companion.External){
            "Danger level22222".makeOfColour(
            staticRule({ value3 < 10 }, Colour.RED),
            staticRule({ value3 <= 21 },  Colour.BRIGHT_MAGENTA),
            staticRule({ value3 > 21 },   Colour.YELLOW),
            )
        }
        item1.print(Item.Companion.External)
    }

}