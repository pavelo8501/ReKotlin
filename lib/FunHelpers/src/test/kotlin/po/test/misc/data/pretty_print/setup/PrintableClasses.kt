package po.test.misc.data.pretty_print.setup

import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.Templated
import po.misc.data.strings.appendGroup
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf

class PrintableElement(
    val elementName: String,
    val parameter: String = "Parameter",
    val value: Int = 1,
):Templated<PrintableElement>{

    override val receiverType: TypeToken<PrintableElement> = tokenOf()

}

class PrintableNested(
    val nested: String = "PrintableSecondNested",
): PrettyPrint{

    override val formattedString: String get() = nested.colorize(Colour.Blue)
    override fun toString(): String = "PrintableSecondNested"
}

class PrintableRecordSubClass(
    val subName: String = "Printable record sub name",
    val subComponent: String = "PrintableRecordSubClass component",
    val nested : PrintableNested = PrintableNested()
): PrettyPrint{

    private val string: String =  buildString {
        appendGroup("[", "]", ::subName, ::subComponent)
    }
    override val formattedString: String get() = string.colorize(Colour.Blue)

    override fun toString(): String =
        string
}

class PrintableRecord(
    var name: String = "Personal",
    var component: String = "Component name",
    val description: String = "Some string of the component",
    val subClass: PrintableRecordSubClass = PrintableRecordSubClass(),
    var elements: List<PrintableElement> = emptyList()
): Templated<PrintableRecord> {

    override val receiverType: TypeToken<PrintableRecord> = tokenOf()

    init {
        if(elements.isEmpty()) {
            elements =  buildList {
                add(PrintableElement("Element 1", "Parameter_1"))
                add(PrintableElement("Element 2", "Parameter_2"))
            }
        }
    }
}
