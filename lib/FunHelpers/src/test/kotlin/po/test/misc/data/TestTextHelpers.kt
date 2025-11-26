package po.test.misc.data

import org.junit.jupiter.api.Test
import po.misc.data.messageAssembler
import po.misc.data.output.output
import po.misc.data.toDisplayName
import kotlin.test.assertEquals

class TestTextHelpers {

    fun someFunction(param: Int, vararg parts: Any) : String = messageAssembler(parts){
        it.formatedString
    }

    fun otherFunction(
        first: String,
        message: String,
        article: String,
        count: String
    ) : String = messageAssembler(first, message, article, count){

        it.formatedString
    }

    @Test
    fun `Run some function`(){
        val msg = "First Message with number 20"
        val result = someFunction(10, "First", "Message", "with number", 20)
        assertEquals(msg, result)
        val result2 =  otherFunction("First", "Message", "with", "number 20")
        assertEquals(result, result2)
    }

    @Test
    fun `RoDisplayName extension`(){
        val shouldBecome = "Some property"
        val propertyName = "someProperty"
        val converted =  propertyName.toDisplayName()
        assertEquals(shouldBecome, converted)
        converted.output()
    }


}