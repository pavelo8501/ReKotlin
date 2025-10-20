package po.test.misc.debugging

import org.junit.jupiter.api.Test
import po.misc.data.helpers.output
import kotlin.reflect.KClass

class TestClassResolver {


    fun <T: Any> someHighestOrderFun(callback: (T) -> Unit) {
        callback::class.simpleName?.output("Simple:")
        callback::class.qualifiedName?.output("Qualified:")
    }

    fun Any.attachedFun() {

       val wildcard =   this::class as KClass<*>


        wildcard.simpleName?.output("Simple:")
        wildcard.qualifiedName?.output("Qualified:")


        this::class.simpleName?.output("Simple:")
        this::class.qualifiedName?.output("Qualified:")
    }

    @Test
    fun `How much info can be obtained from lambda`(){

        someHighestOrderFun<String>{

        }

        attachedFun()



    }

}