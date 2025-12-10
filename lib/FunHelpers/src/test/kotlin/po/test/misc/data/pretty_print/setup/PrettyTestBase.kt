package po.test.misc.data.pretty_print.setup

import po.misc.collections.repeatBuild
import po.misc.data.PrettyPrint
import po.misc.data.pretty_print.Templated
import po.misc.data.strings.appendGroup
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize

abstract class PrettyTestBase : Templated {

    enum class Cell { Cell1, Cell2, Cell3, Cell4 }
    enum class Row { Row1, Row2,  SubTemplateRow }
    enum class Grid { Grid1, Grid2, SubTemplateGrid }

    protected val headerText1: String = "header_text_1"
    protected val headerText2: String = "header_text_2"

    protected val templateHeaderText1 = "template_header_text_1"
    protected val templateHeaderText2 = "template_header_text_2"

    protected val footerText = "Templates footer"

    class PrintableElement(
        val elementName: String,
        val parameter: String = "Parameter",
        val value: Int = 1,
    ):Templated

    class PrintableRecordSubClass(
        val subName: String = "PrintableRecordSubClass",
        val subComponent: String = "PrintableRecordSubClass component",
    ): PrettyPrint{

        private val string: String =  buildString {
            appendGroup("[", "]", ::subName, ::subComponent)
        }

        override val formattedString: String
            get() = string.colorize(Colour.Blue)

        override fun toString(): String =
            string
    }

    class PrintableRecord(
        val name: String = "PersonalName",
        val component: String = "Component name",
        val description: String = "Some description of the component",
        val subClass: PrintableRecordSubClass = PrintableRecordSubClass(),
        var elements: List<PrintableElement> = emptyList()
    ): Templated {

        init {
            if(elements.isEmpty()) {
                elements =  buildList {
                    add(PrintableElement("Element 1", "Parameter_1"))
                    add(PrintableElement("Element 2", "Parameter_2"))
                }
            }
        }
    }

    fun createRecord(elementsCount : Int):PrintableRecord{
       val elements =  elementsCount.repeatBuild {
            PrintableElement("Element $it", "Parameter_$it", it)
        }
       return PrintableRecord(elements = elements)
    }
    fun createRecord():PrintableRecord{
        return PrintableRecord()
    }

}