package po.test.misc.containers

import org.junit.jupiter.api.Test
import po.misc.containers.ReceiverContainer
import po.misc.containers.withReceiver
import po.test.misc.setup.Containable
import kotlin.test.assertIs


class TestReceivableContainer: Containable {

    internal  class Container<T:Containable>(
        override val receiver: T
    ):Containable by  receiver, ReceiverContainer<T>

    @Test
    fun `withReceiver preserve this context`(){

        val someContainingClass = Container<TestReceivableContainer>(this)

        someContainingClass.withReceiver{
            hello()
            assertIs<TestReceivableContainer>(this)
        }

    }
}