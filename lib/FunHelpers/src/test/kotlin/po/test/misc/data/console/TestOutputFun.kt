package po.test.misc.data.console

import org.junit.jupiter.api.Test
import po.misc.data.PrettyPrint
import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize

class TestOutputFun {


    class PrettyClass(
        val simpleStr: String = "Simple Text"
    ): PrettyPrint{

        override val formattedString: String = "simpleStr".colorize(Colour.YellowBright)
    }


    @Test
    fun `PrettyPrint overwrites print functionality`(){
        val pretty = PrettyClass()
        pretty.output()
    }





}