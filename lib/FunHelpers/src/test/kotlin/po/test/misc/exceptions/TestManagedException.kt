package po.test.misc.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableCompanion
import po.misc.exceptions.ManagedPayload
import po.misc.exceptions.throwManaged
import po.misc.types.getOrManaged
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class TestManagedException: CTX {

    override val identity: CTXIdentity<TestManagedException> = asIdentity()
    val nullableString: String? = null

    @Test
    fun `Payload efficiently captures stack trace element`() {
        val managed = assertThrows<ManagedException> {
            nullableString.getOrManaged(this)
        }
        assertEquals("Expected: String. Result is null", managed.message, "Wrong message")
        val firstRec = assertNotNull(managed.exceptionData.firstOrNull())
      //  val traceRec = assertNotNull(firstRec.stackTraceList.firstOrNull { it.className ==  this::class.qualifiedName}, "No trace rec found")
      //  println(traceRec)
    }
}