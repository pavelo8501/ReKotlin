package po.test.misc.containers

import org.junit.jupiter.api.Test
import po.misc.containers.Containable
import po.misc.containers.ReceiverContainer
import po.misc.containers.withReceiver
import kotlin.test.assertIs


internal fun <T: Containable> T.containersMethod():T{
    return this
}

internal fun <T: Containable> T.containersMethod2():T{
    return this
}

class TestReceivableContainer: Containable {

    internal  class Container<T: Any>(
        override val receiver: T
    ):ReceiverContainer<T>{


    }

    @Test
    fun `withReceiver preserve this context`(){

        val someContainingClass = Container<TestReceivableContainer>(this)

        someContainingClass.withReceiver{

            val result = containersMethod()
            val result2 =  containersMethod2()
            assertIs<TestReceivableContainer>(this)
        }

    }
}