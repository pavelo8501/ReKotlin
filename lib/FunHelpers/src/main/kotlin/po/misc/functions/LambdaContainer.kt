package po.misc.functions


import po.misc.functions.interfaces.CallableContainer
import po.misc.functions.interfaces.Containable
import po.misc.interfaces.CtxId
import po.misc.types.TaggedType
import po.misc.types.getOrManaged


abstract class LambdaContainer<T, R, E: Enum<E>>(
    val typedTag: TaggedType<T, E>,
): CallableContainer<T, R> where T:Any, R:Any?{

    override val callback: ((Containable<T>)-> R)? = null
    override val expiryCounter: Int = -1
    override val expires: Boolean get() {
        return expiryCounter == 0
    }
    private var containerData: T? = null

    override fun trigger(data: T){
        containerData = data
        callback?.invoke(this)
    }

    override fun getData():T{
        return containerData.getOrManaged("Container's data is null")
    }
}