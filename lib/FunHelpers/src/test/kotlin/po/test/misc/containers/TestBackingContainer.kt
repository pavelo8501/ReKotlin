package po.test.misc.containers

import org.junit.jupiter.api.Test
import po.misc.containers.LazyContainer
import po.misc.types.TypeData
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestBackingContainer {

    @Test
    fun `LazyBackingContainer work`() {

        val expectedResult = "Result"
        var triggerCount: Int = 0

        val container = LazyContainer<String>(TypeData.create())

        var actualResult1 = ""
        container.requestValue(this){
            actualResult1 = it
            triggerCount++
        }

        container.provideValue(expectedResult)
        container.provideValue("Other Result")

        assertEquals(expectedResult, actualResult1)
        assertTrue(triggerCount == 1)

        var actualResult2 = ""
        container.requestValue(this) {
            actualResult2 = it
        }
        assertEquals(expectedResult, actualResult2)
        assertTrue(triggerCount == 1)
    }
}