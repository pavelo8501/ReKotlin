package po.test.misc.data.pretty_print.setup

import po.misc.collections.repeatBuild

abstract class PrettyTestBase {

    enum class Template { Template1, Template2 }
    enum class CellTemplate { Cell1, Cell2 }

    class PrintableElement(
        val elementName: String,
    )

    class PrintableRecordSubClass(
        val subName: String = "PrintableRecordSubClass",
        val subComponent: String = "PrintableRecordSubClass component",
    )

    class PrintableRecord(
        val name: String = "PersonalName",
        val component: String = "Component name ",
        val description: String = "Some description of the component",
        val subClass: PrintableRecordSubClass = PrintableRecordSubClass(),
        var elements: List<PrintableElement> = emptyList()
    ){
        init {
            if(elements.isEmpty()) {
                elements = listOf(PrintableElement("Element 1"), PrintableElement("Element 2"))
            }
        }
    }

    fun createRecord(elementsCount : Int):PrintableRecord{
       val elements =  elementsCount.repeatBuild {
            PrintableElement("Element $it")
        }
       return PrintableRecord(elements = elements)
    }


    fun createRecord():PrintableRecord{
        return PrintableRecord()
    }

}