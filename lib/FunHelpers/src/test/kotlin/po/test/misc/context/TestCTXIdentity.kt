package po.test.misc.context

import org.junit.jupiter.api.Test
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.context.asSubIdentity
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestCTXIdentity: CTX {

    override val identity: CTXIdentity<out CTX> = asIdentity()

    internal class SubContext(private val testClass: TestCTXIdentity): CTX{
        override val identity: CTXIdentity<out CTX> = asSubIdentity(this, testClass)
    }

    @Test
    fun `Identity provide correct info`(){
        println(identity.classQualifiedName)
        assertEquals("TestCTXIdentity", contextName)

        val subClass = SubContext(this)
        with(subClass){
            assertEquals("SubContext/TestCTXIdentity", completeName)
        }
    }

    @Test
    fun `Identity comparison work as expected`(){
        val subClass = SubContext(this)
        val subIdentityHashCode = subClass.identity.hashCode()
        val identityHashCode = identity.hashCode()
        println(subIdentityHashCode)
        println(identityHashCode)
        assertNotEquals(subIdentityHashCode, identityHashCode)
    }

    @Test
    fun `Identity numeric id work as expected`(){
         val inputID: Long = 10
         identity.setId(inputID)

        assertTrue(identity.isIdUsedDefined)
        assertEquals(inputID, identity.numericId)
        identity.setId(1000)
        assertTrue(identity.numericId == inputID, "Re setting id should have no effect")

        val subClass = SubContext(this)
        subClass.identity.numericId
        subClass.identity.setId(inputID)
        assertTrue(subClass.identity.numericId != inputID, "Re setting id after read should have no effect")
    }

    @Test
    fun `Identity naming pattern work as expected`(){
        val inputID: Long = 10
        val subClass = SubContext(this)
        subClass.identity.setId(inputID)

        assertEquals("SubContext#$inputID",subClass.identity.identifiedByName)
        val subClass2 = SubContext(this)
        val inputID2 : Long= 5000
        subClass2.identity.setId(inputID2)
        subClass2.identity.setNamePattern {usedIdentity->
            "${usedIdentity.completeName}[${usedIdentity.numericId}]"
        }

        assertEquals("${subClass2.identity.completeName}[$inputID2]", subClass2.identity.identifiedByName)
    }
}