package po.misc.types

import kotlin.reflect.KType



interface TypeHolder<T> : ClassAware<T> {
    val kType: KType
    val isCollection: Boolean get() = kType.classifier == List::class
}