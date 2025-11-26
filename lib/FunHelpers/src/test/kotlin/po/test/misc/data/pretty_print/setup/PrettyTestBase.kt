package po.test.misc.data.pretty_print.setup

abstract class PrettyTestBase {


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
            elements = listOf(PrintableElement("Element 1"), PrintableElement("Element 2"))
        }
    }

    fun createRecord():PrintableRecord{
        return PrintableRecord()
    }

}