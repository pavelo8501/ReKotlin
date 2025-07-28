package po.test.misc.setup

import po.misc.containers.ReceiverContainer
import po.test.misc.containers.TestBackingContainer


interface Containable{
    fun hello(){
        println("hello")
    }
}



internal class ContainingClass<T:  Containable>(
    override val receiver: T
):Containable by  receiver, ReceiverContainer<T>{

}