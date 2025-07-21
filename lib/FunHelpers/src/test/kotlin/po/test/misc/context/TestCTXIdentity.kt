package po.test.misc.context

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.context.subIdentity
import kotlin.test.assertEquals

class TestCTXIdentity: CTX {

    override val identity: CTXIdentity<out CTX> = asIdentity()

    internal class SubContext(private val testClass: TestCTXIdentity): CTX{
        override val identity: CTXIdentity<out CTX> = subIdentity(this,testClass)
    }

    @Test
    fun `Identity provide correct info`(){

        println(identity.qualifiedName)
        assertEquals("TestCTXIdentity", contextName)

        val subClass = SubContext(this)
        with(subClass){
            assertEquals("SubContext/TestCTXIdentity", completeName)
        }
    }

}