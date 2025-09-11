package po.misc.callbacks


import po.misc.context.CTX
import po.misc.context.Identifiable
import po.misc.types.getOrManaged

interface Containable<T: Any>{
    val expiryCounter: Int
    val subscriber: CTX
    fun getData():T
}

interface CallableContainer<T: Any>: Containable<T>{
    val  createdBy: CallbackPayloadBase<*,*,*>
    override val expiryCounter: Int
    override val subscriber: CTX
    val callback: (Containable<T>)-> Unit
    val routingInfo : MutableList<HopInfo>
    val expires: Boolean

    fun trigger(data :T)
}

class RoutedWithConversionContainer<T1, T2> internal constructor(
    override val  createdBy: CallbackPayloadBase<*,*, *>,
    override val subscriber: CTX,
    override val expiryCounter: Int,
    val dataAdapter: (T2)->T1,
    override val callback: (Containable<T1>)-> Unit
):CallableContainer<T1> where T1:Any, T2: Any {

    override val expires: Boolean get() {
        return expiryCounter == 0
    }

    override val routingInfo : MutableList<HopInfo> = mutableListOf()
    val thisHop : HopInfo get() = routingInfo.last()

    private var containerData: T1? = null

    fun addHopInfo(sender: CTX, receiver: CallableContainer<*>):RoutedWithConversionContainer<T1, T2>{
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
        return containerData.getOrManaged(this, Any::class)
    }
}


class RoutedContainer<T> internal constructor(
    override val  createdBy: CallbackPayloadBase<*,*, *>,
    override val subscriber: CTX,
    override val expiryCounter: Int,
    //val dataAdapter: (T2)->T1,
    override val callback: (Containable<T>)-> Unit
):CallableContainer<T> where T:Any{

    override val expires: Boolean get() {
        return expiryCounter == 0
    }

    override val routingInfo : MutableList<HopInfo> = mutableListOf()
    val thisHop : HopInfo get() = routingInfo.last()

    private var containerData: T? = null

    fun addHopInfo(sender: CTX, receiver: CallableContainer<*>):RoutedContainer<T>{
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

    override fun trigger(data: T){
        thisHop.dataName = data::class.simpleName.toString()
        containerData = data
        callback.invoke(this)
    }
    override fun getData():T{
        return containerData.getOrManaged(this, Any::class)
    }
}

/**
 * Will include routing information for complex transfers and other arbitrary data
 * Additionally can be extended with lambda with receiver variations
 * Later can be interchanged to Flow based container
 */
class CallbackContainer<T> internal constructor (
    override val createdBy: CallbackPayloadBase<*,*,*>,
    override val subscriber: CTX,
    override val expiryCounter: Int,
    override val callback: ((Containable<T>)-> Unit)
): CallableContainer<T> where T:Any{

    override val expires: Boolean get() {
       return expiryCounter == 0
    }

    private var containerData: T? = null

    override val routingInfo : MutableList<HopInfo> = mutableListOf()

    override fun trigger(data: T){
        containerData = data
        callback.invoke(this)
    }

    override fun getData():T{
        return containerData.getOrManaged(this, Any::class)
    }
}

