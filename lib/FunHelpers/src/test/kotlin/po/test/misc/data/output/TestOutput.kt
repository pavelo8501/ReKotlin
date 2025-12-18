package po.test.misc.data.output

import po.misc.data.output.LocateOutputs
import po.misc.data.output.output
import kotlin.test.Test

class TestOutput {


    @Test
    fun `Orphan output functions can be located`(){

        output("Prefix")
        output(LocateOutputs)
    }


}