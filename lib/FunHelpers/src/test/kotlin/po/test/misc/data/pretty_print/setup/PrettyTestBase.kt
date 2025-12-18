package po.test.misc.data.pretty_print.setup

import po.misc.collections.repeatBuild
import po.misc.data.pretty_print.Templated
import po.misc.data.pretty_print.parts.RowID
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf



abstract class PrettyTestBase : Templated<PrintableRecord>, TokenFactory {

    override val valueType: TypeToken<PrintableRecord> = tokenOf()

    enum class Cell: RowID { Cell1, Cell2, Cell3, Cell4 }
    enum class Row: RowID { Row1, Row2,  SubTemplateRow }
    enum class Grid: RowID { Grid1, Grid2, SubTemplateGrid }

    protected val headerText1: String = "header_text_1"
    protected val headerText2: String = "header_text_2"

    protected val templateHeaderText1 = "template_header_text_1"
    protected val templateHeaderText2 = "template_header_text_2"

    protected val footerText = "Templates footer"

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