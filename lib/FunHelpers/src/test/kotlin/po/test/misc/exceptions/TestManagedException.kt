package po.test.misc.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import po.misc.exceptions.name
import po.misc.exceptions.shortName
import kotlin.test.assertEquals

class TestManagedException {

    enum class CustomExceptionCode{
        Unknown,
        Code1,
        Code2
    }

    class CustomException(
        override var message: String,
        source:CustomExceptionCode,
        original: Throwable?
    ) : ManagedException(message, source, original){
        override var handler: HandlerType = HandlerType.SkipSelf

        companion object : ManageableException.Builder<CustomException> {
            override fun build(message: String,  source: Enum<*>?, original: Throwable?): CustomException {
                val sourceEnum = source as CustomExceptionCode
                return CustomException(message, source, original)
            }
        }
    }

    fun throwCustomException(message: String, code:CustomExceptionCode): Nothing{
       throw ManageableException.build<CustomException>(message, code)
    }

    @Test
    fun `correct exception type is thrown`(){
        val message  = "text"
        val exception =  assertThrows<CustomException> {
            throwCustomException(message, CustomExceptionCode.Code1)
        }
        assertEquals(message, exception.message, "Wrong Exception message")
    }

    @Test
    fun `exception name parsed correctly`(){

        val message = "exception text"
        val nameShouldBe  = "CustomException[msg:$message code:${CustomExceptionCode.Code1.name} hdl:SKIP_SELF]"
        val shortNameShouldBe  = "CustomException($message)"
        val exception =  assertThrows<CustomException> {
            throwCustomException(message, CustomExceptionCode.Code1)
        }
        val parsedName = exception.name()
        val parsedShortName = exception.shortName()
        assertEquals(nameShouldBe, parsedName, "ExceptionName wrongly parsed")
        assertEquals(shortNameShouldBe, parsedShortName, "Exception \"ShortName\" wrongly parsed")
    }


}