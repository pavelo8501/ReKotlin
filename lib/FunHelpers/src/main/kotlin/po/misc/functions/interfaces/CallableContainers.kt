package po.misc.functions.interfaces


import po.misc.interfaces.CtxId


interface Containable<T: Any>{
    val expiryCounter: Int
    val subscriber: CtxId
    fun getData():T
}

interface CallableContainer<T: Any, R: Any?>: Containable<T>{

    override val expiryCounter: Int
    override val subscriber: CtxId
    val callback: ((Containable<T>)-> R)?
    val expires: Boolean
    fun trigger(data :T)
}