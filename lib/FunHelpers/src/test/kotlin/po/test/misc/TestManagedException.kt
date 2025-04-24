package po.test.misc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.exceptions.HandlerType
import po.misc.exceptions.ManagedException
import po.misc.exceptions.SelfThrownException
import po.misc.exceptions.castOrThrow
import kotlin.test.assertTrue

class CustomException(
    override var message: String,
    override var handler: HandlerType,
    val optionalParam : Boolean,
) : ManagedException(message, handler){
    override val builderFn: (String, HandlerType) -> CustomException = {message, handler->
        CustomException(message, handler, optionalParam)
    }

    companion object : SelfThrownException.Companion.Builder<CustomException> {
        override fun build(message: String, handler: HandlerType): CustomException =
            CustomException(message, handler, true)
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