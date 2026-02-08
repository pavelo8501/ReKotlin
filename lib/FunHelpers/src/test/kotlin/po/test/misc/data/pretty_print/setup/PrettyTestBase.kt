package po.test.misc.data.pretty_print.setup

import po.misc.collections.repeatBuild
import po.misc.data.logging.Verbosity
import po.misc.data.pretty_print.Templated
import po.misc.data.styles.TextStyler
import po.misc.interfaces.named.Named
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import java.time.Instant


abstract class PrettyTestBase : Templated<PrintableRecord>, TokenFactory {

    protected var verbosity: Verbosity = Verbosity.Debug
    override val receiverType: TypeToken<PrintableRecord> = tokenOf()

    enum class EntyType { Type1, Type2, Type3, Type4 }

    class ReportRecord(
        val text: String = "Text 1",
        val date: Instant = Instant.now(),
    )

    class ReportEntry(
        val name: String,
        val description: String,
        val ok:Boolean,
        val type: EntyType,
        private val count:Int = 1,
        val record: ReportRecord = ReportRecord("Sub entry #${count}"),
    )

    class ReportClass(
        private val entryCount: Int,
        val reportName:String = "Default name",
        val entries: MutableList<ReportEntry> = mutableListOf(),
        val record: ReportRecord = ReportRecord("Record on ReportClass ")
    ){
        init {
            entryCount.repeatBuild {
               val entry = ReportEntry(
                    name = "${reportName}_enty_${it}",
                    description = "${reportName}_enty",
                    ok = true,
                    type = EntyType.Type1,
                )
                entries.add(entry)
            }
        }
    }

    protected val emptyString : String = ""
    protected val headerText2: String = "header_text_2"
    protected val templateHeaderText1 = "template_header_text_1"
    protected val templateHeaderText2 = "template_header_text_2"
    protected val footerText = "Templates footer"

    protected val enableOutput:Boolean = true

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

abstract class PrettyTest<T>(protected var testVerbosity: Verbosity = Verbosity.Debug): Templated<T>, TextStyler, Named {

    override val name: String get() = receiverType.typeName

}