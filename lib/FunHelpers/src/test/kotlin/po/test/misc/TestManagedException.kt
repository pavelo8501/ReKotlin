package po.test.misc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.types.castOrThrow
import kotlin.test.assertTrue

class CustomException(
    override var message: String,
    override var handler: HandlerType,
    val optionalParam : Boolean,
) : ManagedException(message){


    companion object : SelfThrownException.Companion.Builder<CustomException> {


        override fun build(message: String, optionalCode: Int?): CustomException {
            val handlerType = HandlerType.fromValue(optionalCode!!)
           return CustomException(message, handlerType, true)
        }
    }
}

class TestManagedException {

    @Test
    fun `correct exception type is thrown`(){
        val string : String = "text"
        val ex =  assertThrows<CustomException> {
           val casted = string.castOrThrow<Boolean, CustomException>()
        }

        assertTrue(ex.message.contains("Unable to cast"), "Wrong message")
    }

}