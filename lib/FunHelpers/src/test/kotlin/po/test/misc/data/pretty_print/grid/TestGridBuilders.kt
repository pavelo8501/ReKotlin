package po.test.misc.data.pretty_print.grid

import po.misc.data.pretty_print.grid.buildPrettyGrid2
import po.test.misc.data.pretty_print.setup.PrettyTestBase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestGridBuilders : PrettyTestBase(){


    @Test
    fun `Property return type of V and List V can be safely distinguished`(){
        val container = buildPrettyGrid2(PrintableRecord::elements){property->

        }

        assertTrue { container.type.isCollection }
       // assertNotNull(container.listLoader.property)

    }



}