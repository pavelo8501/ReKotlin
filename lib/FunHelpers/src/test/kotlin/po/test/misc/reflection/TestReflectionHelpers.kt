package po.test.misc.reflection

import org.junit.jupiter.api.Test
import po.misc.reflection.MetaContainer

class TestReflectionHelpers : MetaContainer {

    val someProperty : String = "value"

    @Test
    fun `Test meta properties`(){

        var inMethodProperty : Int = 10
        inMethodProperty = 20

    }

}