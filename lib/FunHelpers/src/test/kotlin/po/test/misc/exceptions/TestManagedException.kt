package po.test.misc.exceptions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManageableException
import po.misc.exceptions.toManaged
import po.misc.exceptions.toPayload
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asContext
import kotlin.test.assertEquals

class TestManagedException: CTX {

    enum class CustomExceptionCode{
        Unknown,
        Code1,
        Code2
    }

    override val identity: CTXIdentity<out CTX> = asContext()

    override val contextName: String
        get() = "TestManagedException"


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
       throw ManageableException.build<CustomException, CustomExceptionCode>(message, code)
    }


    @Test
    fun `Waypoint data from stacktrace`(){
        val exPayload = this.toPayload("Test")
        val exception =  assertThrows<ManagedException> {
            throw exPayload.toManaged()
        }
        val trace = exception.stackTrace
        trace.forEach { println(it) }
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
    }


}