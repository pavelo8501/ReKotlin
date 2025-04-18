package po.test.lognotify.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.exceptions.ManagedException
import po.lognotify.exceptions.enums.HandlerType
import po.lognotify.extensions.getOrException
import po.lognotify.extensions.testOrException
import kotlin.test.assertEquals


class CancelException(
    message: String,
    handler: HandlerType = HandlerType.CANCEL_ALL
) : ManagedException(message, handler) {
    override val builderFn: (String) -> CancelException
        get() = ::CancelException
}

class SkipException(
    message: String,
    handler: HandlerType = HandlerType.SKIP_SELF
) : ManagedException(message, handler) {
    override val builderFn: (String) -> SkipException
        get() = ::SkipException
}

fun <T: Any> T?.getOrThrowSkip(message: String, handler: HandlerType): T{
   return  this.getOrException(SkipException(message, handler))
}

fun <T: Any> T?.getOrThrowCancel(message: String): T{
    return  this.getOrException(CancelException(message, HandlerType.CANCEL_ALL))
}

class TestManagedException {

    @Test
    fun `derived SkipException can be thrown`(){
        var testVal : String? = "value"
        val resulting = testVal.getOrThrowSkip("skip", HandlerType.SKIP_SELF)
        assertEquals("value", resulting, "GetOrThrowSkip failed")

        testVal = null
        val thrown =  assertThrows<SkipException> {
            testVal.getOrThrowSkip("skip", HandlerType.SKIP_SELF)
        }
        assertEquals("skip", thrown.message, "Message mismatch in skip exception")
        assertEquals(HandlerType.SKIP_SELF, thrown.handler, "Skip exception handler mismatch")
    }

    @Test
    fun `derived CancelException can be thrown`(){
        var testVal : String? = "value"
        val resulting = testVal.getOrThrowCancel("cancel")
        assertEquals("value", resulting, "GetOrThrowSkip failed")

        testVal = null
        val thrown =  assertThrows<CancelException> {
            testVal.getOrThrowCancel("cancel")
        }
        assertEquals("cancel", thrown.message, "Message mismatch in cancel exception")
        assertEquals(HandlerType.CANCEL_ALL, thrown.handler, "Cancel exception handler mismatch")
    }

    @Test
    fun `testOrException throws correctly`(){

        val testVal : String? = "value"
        val resulting = testVal.testOrException(CancelException("cancel", HandlerType.CANCEL_ALL)){
            it == "value"
        }
        assertEquals("value", resulting, "TestOrException failed")

        val thrown =  assertThrows<CancelException> {
            testVal.testOrException(CancelException("cancel", HandlerType.CANCEL_ALL)){
                it == "something else"
            }
        }
        assertEquals("cancel", thrown.message, "Message mismatch in cancel exception")
        assertEquals(HandlerType.CANCEL_ALL, thrown.handler, "Cancel exception handler mismatch")

        val thrownSkip =  assertThrows<SkipException> {
            testVal.testOrException(SkipException("skip", HandlerType.SKIP_SELF)){
                false
            }
        }
        assertEquals("skip", thrownSkip.message, "Message mismatch in skip exception")
        assertEquals(HandlerType.SKIP_SELF, thrownSkip.handler, "Skip exception handler mismatch")

    }

}