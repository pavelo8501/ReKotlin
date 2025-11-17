package po.test.misc.types

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.ExceptionPayload
import po.misc.types.castOrThrow
import po.misc.types.getOrThrow
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestHelpers: TraceableContext {

    var nullableString: String? = null

    internal class TestHelpersSubClass(val parentClass:TestHelpers){
        var nullable: Int? = null
        var exceptionPayload: ExceptionPayload? = null

        fun throwingMethod(){
            nullable.getOrThrow(parentClass){
                exceptionPayload = it
                Exception(it.message)
            }
        }
        fun castThrowingMethodOnNull(){
            nullable.castOrThrow<String>(parentClass){
                exceptionPayload = it
                Exception(it.message)
            }
        }
        fun castThrowingMethod(nullableIntValue: Int){
            nullable = nullableIntValue
            nullable.castOrThrow<String>(parentClass){
                exceptionPayload = it
                Exception(it.message)
            }
        }
    }

    @Test
    fun `getOrThrow correctly resolves context`(){
       var exceptionPayload: ExceptionPayload? = null
       val exception = assertThrows<Exception> {
            nullableString.getOrThrow(String::class){payload->
                exceptionPayload = payload
                Exception(payload.message)
            }
        }
        val payload = assertNotNull(exceptionPayload)
        assertEquals("getOrThrow", payload.methodName)
        assertEquals(exception.message, payload.message)
        assertTrue {
            payload.message.contains("String")
        }

        val subClass = TestHelpersSubClass(this)
        val subClassException = assertThrows<Exception> {
            subClass.throwingMethod()
        }
        val subClassPayload = assertNotNull(subClass.exceptionPayload)
        assertEquals("getOrThrow", subClassPayload.methodName)
        assertEquals(subClassException.message, subClassPayload.message)
        assertTrue {
            subClassPayload.message.contains("Int")
        }
    }

    @Test
    fun `castOrThrow correctly resolves context`(){
        nullableString = "Some string"
        var exceptionPayload: ExceptionPayload? = null
        val exception = assertThrows<Exception> {
            nullableString.castOrThrow<Int>(this@TestHelpers){
                exceptionPayload = it
                Exception(it.message)
            }
        }
        val payload = assertNotNull(exceptionPayload)
        assertEquals("castOrThrow", payload.methodName)
        assertEquals(exception.message, payload.message)
        assertTrue {
            payload.message.contains("Int") &&
                    payload.message.contains("cannot be cast")
        }
        assertIs<ClassCastException>(payload.cause)

        val subClass = TestHelpersSubClass(this)
        val subClassException = assertThrows<Exception> {
            subClass.castThrowingMethod(10)
        }
        val subClassPayload = assertNotNull(subClass.exceptionPayload)
        assertEquals("castOrThrow", subClassPayload.methodName)
        assertEquals(subClassException.message, subClassPayload.message)
        assertTrue {
            subClassPayload.message.contains("String") &&
                    subClassPayload.message.contains("cannot be cast")
        }
    }

    @Test
    fun `castOrThrow (null case) correctly resolves context`(){
        var exceptionPayload: ExceptionPayload? = null
        val exception = assertThrows<Exception> {
            nullableString.castOrThrow<Int>(this){
                exceptionPayload = it
                Exception(it.message)
            }
        }
        val payload = assertNotNull(exceptionPayload)
        assertEquals("castOrThrow", payload.methodName)
        assertEquals(exception.message, payload.message)
        assertTrue {
            payload.message.contains("Expected Int")
        }
        val subClass = TestHelpersSubClass(this)
        val subClassException = assertThrows<Exception> {
            subClass.castThrowingMethodOnNull()
        }
        val subClassPayload = assertNotNull(subClass.exceptionPayload)
        assertEquals("castOrThrow", subClassPayload.methodName)
        assertEquals(subClassException.message, subClassPayload.message)
        assertTrue {
            subClassPayload.message.contains("Expected String")
        }
    }
}