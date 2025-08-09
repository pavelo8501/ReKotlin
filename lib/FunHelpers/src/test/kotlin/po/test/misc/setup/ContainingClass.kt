package po.test.misc.setup

import po.misc.containers.Containable
import po.misc.containers.ReceiverContainer



internal class ContainingClass<T: Containable>(
    override val receiver: T
):Containable by  receiver, ReceiverContainer<T>{

}