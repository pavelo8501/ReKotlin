package po.misc.functions.registries



interface LambdaSubscriber<V: Any> {
    val subscriberID: Long
    fun trigger(value:V)
}