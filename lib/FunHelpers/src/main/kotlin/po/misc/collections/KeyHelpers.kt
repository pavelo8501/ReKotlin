package po.misc.collections

import po.misc.interfaces.Identifiable

fun <SO : Identifiable, E : Enum<E>> SO.generateKey(parameter: E): CompositeEnumKey<SO, E> {
    return CompositeEnumKey(this, parameter)
}