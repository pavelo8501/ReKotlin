package po.test.misc.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.ManagedException
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.PrintableCompanion
import po.misc.exceptions.models.ExceptionData2
import po.misc.exceptions.throwManaged
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestManagedException: CTX {

    override val identity: CTXIdentity<TestManagedException> = asIdentity()

   internal class ArbitraryData(
        val producer: CTX,
    ): PrintableBase<ArbitraryData>(ExceptionTemplate){

        override val self: ArbitraryData =  this

        companion object: PrintableCompanion<ArbitraryData>({ ArbitraryData::class}){

          val ExceptionTemplate = createTemplate{
                next {
                    producer.contextName
                }
            }
        }

    }

    @Test
    fun `ExceptionData logic work as expected`(){
        val exception = assertThrows<ManagedException> {
            throwManaged("Some text")
        }
        val auxData = ArbitraryData(this)
        val data =  ExceptionData2(ManagedException.ExceptionEvent.Rethrown, "Rethrow text", this)
        exception.addExceptionData(data)
        assertTrue(exception.stackTrace.isNotEmpty())
        assertTrue(exception.exceptionData.isNotEmpty())
        assertEquals(2, exception.exceptionData.size)
        val exceptionRecord = exception.exceptionData[1]
        assertNotNull(exceptionRecord.thisStackTraceElement)
    }


}