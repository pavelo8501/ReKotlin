package po.test.misc.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.types.getOrManaged
import kotlin.test.assertEquals


class TestManagedException: CTX {

    override val identity: CTXIdentity<TestManagedException> = asIdentity()
    val nullableString: String? = null

    @Test
    fun `Payload efficiently captures stack trace element`() {
        val managed = assertThrows<ManagedException> {
            nullableString.getOrManaged(this)
        }
        assertEquals("Expected: String. Result is null", managed.message, "Wrong message")
    }
}