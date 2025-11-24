package po.test.misc.types.k_class

import org.junit.jupiter.api.Test
import po.misc.data.logging.models.LogMessage
import po.misc.data.printable.Printable
import po.misc.types.k_class.computeHierarchy
import kotlin.test.assertEquals

class TestKClassHelpers {

    @Test
    fun `ClassHierarchyMap resolves hierarchy and tops precisely by depth`(){

        val result = LogMessage::class.computeHierarchy(5)
        assertEquals(5, result.size)
        assertEquals(LogMessage::class,  result.first())
        assertEquals(Printable::class,  result.last())
    }

    @Test
    fun `ClassHierarchyMap resolves hierarchy and stops on given lower bound even if depth is larger`(){
        val result = LogMessage::class.computeHierarchy(10, stopBefore = Any::class)
        assertEquals(5, result.size)
        assertEquals(LogMessage::class,  result.first())
        assertEquals(Printable::class,  result.last())
    }


    @Test
    fun `ClassHierarchyMap resolves hierarchy and stops on Any even if lower bound is misused`(){
        val result = LogMessage::class.computeHierarchy(10, stopBefore = String::class)
        assertEquals(6, result.size)
        assertEquals(LogMessage::class,  result.first())
        assertEquals(Any::class,  result.last())
    }

}