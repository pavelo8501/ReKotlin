package po.test.misc.types.token

import org.junit.jupiter.api.Test
import po.misc.data.logging.Loggable
import po.misc.data.logging.models.LogMessage
import po.misc.data.printable.Printable
import po.misc.types.ClassHierarchyMap
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf
import kotlin.test.assertEquals

class TestTypeTokenFeatures: TokenFactory {


    @Test
    fun `HierarchyMap integration and usage`(){

        val token =  tokenOf<LogMessage>(TypeToken.Options(5))
        assertEquals(5, token.classHierarchyMap.hierarchyCache.size)
        with(token.classHierarchyMap.hierarchyCache){
            assertEquals(LogMessage::class,  first())
            assertEquals(Printable::class,  last())
        }

        val token2 =  tokenOf<LogMessage>(TypeToken.Options(5, scanBeforeClass = Printable::class))
        assertEquals(4,  token2.classHierarchyMap.hierarchyCache.size)
        with(token2.classHierarchyMap.hierarchyCache){
            assertEquals(LogMessage::class,  first())
            assertEquals(Loggable::class,  last())
        }
    }
}