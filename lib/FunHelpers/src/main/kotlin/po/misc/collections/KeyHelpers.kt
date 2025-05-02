package po.misc.collections

fun <SO : Identifiable, E : Enum<E>> SO.generateKey(parameter: E): CompositeKey<SO, E> {
    return CompositeKey(this, parameter)
}