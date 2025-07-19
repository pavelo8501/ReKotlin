package po.misc.functions.interfaces

import po.misc.context.CTX


interface Containable<T: Any>{
    val expiryCounter: Int
    val subscriber: CTX
    fun getData():T
}

interface CallableContainer<T: Any, R: Any?>: Containable<T>{

    override val expiryCounter: Int
    override val subscriber: CTX
    val callback: ((Containable<T>)-> R)?
    val expires: Boolean
    fun trigger(data :T)
}