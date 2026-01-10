package po.test.misc.data.pretty_print.parts

import po.misc.types.token.TypeToken
import po.misc.types.token.TypeToken.Companion.invoke
import kotlin.test.Test
import kotlin.test.assertIs

class TestTypedKeys {

    class TestKeyGeneric<T>(val token: TypeToken<T>)

    class TestKey(){
        companion object{
            inline operator fun <reified T> invoke(): TestKeyGeneric<T>{
                return  TestKeyGeneric(TypeToken<T>())
            }
        }
    }


    @Test
    fun `TestKey madness`() {
        val result = TestKey<String>()
        assertIs<TestKeyGeneric<String>>(result)
    }
}