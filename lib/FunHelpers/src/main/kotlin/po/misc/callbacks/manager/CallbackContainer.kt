package po.misc.callbacks.manager


import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.IdentifiableContext
import po.misc.types.getOrManaged

interface Containable<T: Any>{
    val expires: Boolean
    val subscriber: IdentifiableClass

    fun getData():T
}

interface CallableContainer<T: Any>: Containable<T>{
    val  createdBy: CallbackPayloadBase<*,*,*>
    override val expires: Boolean
    override val subscriber: IdentifiableClass
    val callback: (Containable<T>)-> Unit
    val routingInfo : MutableList<HopInfo>

    fun trigger(data :T)
}

class RoutedContainer<T1, T2> internal constructor(
    override val  createdBy: CallbackPayloadBase<*,*,*>,
    override val expires: Boolean,
    override val subscriber: IdentifiableClass,
    val dataAdapter: (T2)->T1,
    override val callback: (Containable<T1>)-> Unit
):CallableContainer<T1> where T1:Any, T2: Any {

    override val routingInfo : MutableList<HopInfo> = mutableListOf()

    val thisHop : HopInfo get() = routingInfo.last()

    private var containerData: T1? = null

    fun addHopInfo(sender: IdentifiableContext, receiver: CallableContainer<*>):RoutedContainer<T1, T2>{
        routingInfo.addAll(receiver.routingInfo)
        val info =  HopInfo(
            emitterName =  sender.contextName,
            receiverName = createdBy.manager.emitter.contextName,
            subscriber = subscriber,
            dataName = "",
            hopNr = routingInfo.size
        )
        routingInfo.add(info)
        return this
    }

    override fun trigger(data: T1){
        containerData = data
        callback.invoke(this)
    }

    fun triggerRouted(data: T2){
        thisHop.dataName = data::class.simpleName.toString()
        val converted = dataAdapter.invoke(data)
        containerData = converted
        callback.invoke(this)
    }

    override fun getData():T1{
        return containerData.getOrManaged("Container's data is null")
    }
}

/**
 * Will include routing information for complex transfers and other arbitrary data
 * Additionally can be extended with lambda with receiver variations
 * Later can be interchanged to Flow based container
 */
class CallbackContainer<T> internal constructor (
    override val  createdBy: CallbackPayloadBase<*,*,*>,
    override val expires: Boolean,
    override val subscriber: IdentifiableClass,
    override val callback: ((Containable<T>)-> Unit)
): CallableContainer<T> where T:Any{

    private var containerData: T? = null

    override val routingInfo : MutableList<HopInfo> = mutableListOf()

    override fun trigger(data: T){
        containerData = data
        callback.invoke(this)
    }

    override fun getData():T{
        return containerData.getOrManaged("Container's data is null")
    }

}