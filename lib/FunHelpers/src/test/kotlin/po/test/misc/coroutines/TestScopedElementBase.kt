package po.test.misc.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.ScopedElementBase
import po.misc.coroutines.runElementAsync
import po.misc.coroutines.runElementAwait
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestScopedElementBase {

    class TestElement() : ScopedElementBase<TestElement>() {

        override val identity: CTXIdentity<TestElement> = asIdentity()

        init {
            identity.setNamePattern {
                "TestElement as Context"
            }
        }
        override val key: CoroutineContext.Key<TestElement> get() = Key
        companion object Key : CoroutineContext.Key<TestElement>
    }


    @Test
    fun `Coroutine  async launcher `() = runTest{

        var coroutineName : String? = null
        var fromContext: Any? = null
        val element = TestElement()
        element.runElementAwait {
            val context =currentCoroutineContext()
            fromContext = context[TestElement]
            coroutineName = context[CoroutineName]?.name?:""
        }
        assertIs<TestElement>(fromContext)
        val nameExists =  assertNotNull(coroutineName)
        assertEquals("TestElement as Context", nameExists)
    }

    @Test
    fun `Coroutine  launch `(){
        var coroutineName : String? = null
        var fromContext: Any? = null
        val element = TestElement()
        runBlocking {
            element.runElementAsync {
                val context =currentCoroutineContext()
                fromContext = context[TestElement]
                coroutineName = context[CoroutineName]?.name?:""

            }
        }
        assertIs<TestElement>(fromContext)
        val nameExists =  assertNotNull(coroutineName)
        assertEquals("TestElement as Context", nameExists)
    }

}