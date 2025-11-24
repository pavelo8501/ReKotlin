package po.test.misc.types

import org.junit.jupiter.api.Test
import po.misc.data.logging.models.LogMessage
import po.misc.data.printable.Printable
import po.misc.types.ClassHierarchyMap
import kotlin.test.assertEquals

class TestClassHierarchyMap {

    @Test
    fun `ClassHierarchyMap resolves hierarchy and tops precisely by depth`(){
        val  classHierarchyMap = ClassHierarchyMap(LogMessage::class, 5)
        val result = classHierarchyMap.resolve()
        assertEquals(5, result.size)
        assertEquals(LogMessage::class,  result.first())
        assertEquals(Printable::class,  result.last())
    }

    @Test
    fun `ClassHierarchyMap resolves hierarchy and stops on given lower bound even if depth is larger`(){
        val  classHierarchy = ClassHierarchyMap(LogMessage::class, 10)
        assertEquals(5, classHierarchy.hierarchyCache.size)
        with(classHierarchy.hierarchyCache){
            assertEquals(LogMessage::class,  first())
            assertEquals(Printable::class,  last())
        }
    }

}