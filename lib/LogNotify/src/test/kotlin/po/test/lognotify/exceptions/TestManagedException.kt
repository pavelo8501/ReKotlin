package po.test.lognotify.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.lognotify.exceptions.getOrThrow
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.getOrException
import po.misc.exceptions.testOrException
import kotlin.test.assertEquals
import kotlin.time.measureTime


class CancelException(
    message: String,

) : ManagedException(message) {
    override var handler: HandlerType = HandlerType.SKIP_SELF
    override val builderFn: (String, Int?) -> CancelException = { msg, _ ->
        CancelException(msg)
    }
}

class SkipException(
    message: String,
) : ManagedException(message) {

    override  var handler: HandlerType = HandlerType.SKIP_SELF

    override val builderFn: (String, Int?) -> SkipException = {msg, _->
         SkipException(msg)
    }

}

fun <T: Any> T?.getOrThrowSkip(message: String, handler: HandlerType): T{
   return  this.getOrException {
       (SkipException(message))
   }
}

fun <T: Any> T?.getOrThrowCancel(message: String): T{
    return  this.getOrException{
        (CancelException(message))
    }
}

class TestManagedException {

    @Test
    fun `derived SkipException can be thrown`(){
        var testVal : String? = "value"
        val resulting = testVal.getOrThrow("skip",)
       val res =  assertThrows<SkipException> {
            val resulting = testVal.getOrThrow("skip")
        }
        assertEquals("value", resulting, "GetOrThrowSkip failed")

        testVal = null
        val thrown =  assertThrows<SkipException> {
            testVal.getOrThrow("skip")
        }
        assertEquals("skip", thrown.message, "Message mismatch in skip exception")
        assertEquals(HandlerType.SKIP_SELF, thrown.handler, "Skip exception handler mismatch")
    }

    @Test
    fun `derived CancelException can be thrown`(){
        var testVal : String? = "value"
        val resulting = testVal.getOrThrow("cancel")
        assertEquals("value", resulting, "GetOrThrowSkip failed")

        testVal = null
        val thrown =  assertThrows<CancelException> {
            testVal.getOrThrow("cancel")
        }
        assertEquals("cancel", thrown.message, "Message mismatch in cancel exception")
        assertEquals(HandlerType.CANCEL_ALL, thrown.handler, "Cancel exception handler mismatch")
    }

    @Test
    fun `testOrException throws correctly`(){

        val testVal : String? = "value"
        val resulting = testVal.testOrException(CancelException("cancel")){
            it == "value"
        }
        assertEquals("value", resulting, "TestOrException failed")

        val thrown =  assertThrows<CancelException> {
            testVal.testOrException(CancelException("cancel")){
              it ==  "something else"
            }
        }
        assertEquals("cancel", thrown.message, "Message mismatch in cancel exception")
        assertEquals(HandlerType.CANCEL_ALL, thrown.handler, "Cancel exception handler mismatch")

        val thrownSkip =  assertThrows<SkipException> {
            testVal.testOrException(SkipException("skip")){
                false
            }
        }
        assertEquals("skip", thrownSkip.message, "Message mismatch in skip exception")
        assertEquals(HandlerType.SKIP_SELF, thrownSkip.handler, "Skip exception handler mismatch")

    }

}