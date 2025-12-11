package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.grid.GridValueContainer
import po.misc.data.pretty_print.grid.buildPrettyGrid
import po.test.misc.data.pretty_print.setup.PrettyTestBase



class TestGridBuilders : PrettyTestBase(){
    
    val newGrid = buildPrettyGrid(PrintableRecord::elements) {
        val thisReceiver : GridValueContainer<PrintableRecord, List<PrintableElement>> = this

    }

}