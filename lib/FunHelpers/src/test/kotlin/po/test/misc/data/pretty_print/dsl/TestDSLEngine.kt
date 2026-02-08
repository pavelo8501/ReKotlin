package po.test.misc.data.pretty_print.dsl

import po.misc.data.pretty_print.buildGrid
import po.misc.data.pretty_print.dsl.DSLEngine
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.types.token.TypeToken
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import po.test.misc.data.pretty_print.setup.PrintableRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class TestDSLEngine : PrettyTestBase(){
    
    private val record: PrintableRecord = createRecord()

    @Test
    fun `DSLEngine correctly creates Grid`() {
        val options = RowOptions(Orientation.Horizontal)

        val grid = buildGrid<PrintableRecord>() {}
        assertSame(options, grid.options)
    }

    @Test
    fun `DSLEngine correctly creates Grid by token`() {
        val dslEngine = DSLEngine()
        val options = RowOptions(Orientation.Horizontal)
        val grid = dslEngine.prepareGrid(TypeToken<PrintableRecord>()) {}
        assertSame(options, grid.options)
    }


}