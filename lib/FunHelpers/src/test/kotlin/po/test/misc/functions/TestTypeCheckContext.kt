package po.test.misc.functions

import org.junit.jupiter.api.Test
import po.misc.context.asContext
import po.misc.data.processors.Logger
import po.misc.functions.checkable.checkAndRun
import po.misc.functions.checkable.onType
import po.test.misc.properties.TestBackingDelegate
import po.test.misc.setup.TestLogger
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestTypeCheckContext {
   internal class TempClass(): TestLogger{
        override val identity = asContext()
    }

    @Test
    fun `onType runs match block and fallback independently, all evaluated`(){

        var isTestBackingDelegate: Boolean? = null
        var notTestBackingDelegate: Boolean? = null
        var isLogger: Boolean? = null
        var notLogger: Boolean? = null

        val tempClass = TempClass()

        tempClass.checkAndRun{
            onType<TestBackingDelegate> {
                isTestBackingDelegate = true
                println("This is TestBackingDelegate")
            }.fallback {
                notTestBackingDelegate = false
                println("Nope not TestBackingDelegate")
            }
            onType<TestLogger> {
                isLogger = true
                println("This is Logger")
            }.fallback {
                notLogger = false
                println("Nope not Logger")
            }
        }

        assertNull(isTestBackingDelegate, "TempClass is not of type TestBackingDelegate, but hit")
        val onTypeTestBackingDelegateFallback =  assertNotNull(notTestBackingDelegate, "onType<TestBackingDelegate> fallback never hit")
        val isLoggerResult =  assertNotNull(isLogger, "onType<Logger> never hit")
        assertNull(notLogger, "onType<Logger> fallback hit but should not")
        assertFalse(onTypeTestBackingDelegateFallback, "onType<TestBackingDelegate> produced false result")
        assertTrue(isLoggerResult, "onType<Logger produced false result")
    }

}