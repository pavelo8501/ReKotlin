package po.misc.data.pretty_print.parts.loader

import po.misc.callbacks.callable.ReceiverCallable
import po.misc.data.pretty_print.parts.loader.ListProvider.Companion.invoke
import po.misc.types.token.TypeToken
import po.misc.types.token.asElementType
import kotlin.reflect.KProperty1


inline fun <reified T, reified V> KProperty1<T, List<V>>.toListProvider(): ListProvider<T, V>
        = ListProvider<T, V>(this)

inline fun <T, reified V> KProperty1<T, List<V>>.toListProvider(receiverType: TypeToken<T>): ListProvider<T, V>
        = ListProvider<T, V>(receiverType, this)

fun <T, V> KProperty1<T, List<V>>.toListProvider(receiverType: TypeToken<T>, valueType: TypeToken<V>): ListProvider<T, V>
        = ListProvider<T, V>(receiverType,  valueType, this)

inline fun <reified T, reified V> Function1<T, List<V>>.toListProvider(): ListProvider<T, V>
        = ListProvider<T, V>(this)

fun <T, V> ListProvider<T, V>.createLoader(name: String): DataLoader<T, V>{
    return  DataLoader(name, sourceType, receiverType.asElementType()).apply(this)
}

fun <T, V> ReceiverCallable<T, List<V>>.createProvider(): ListProvider<T, V>{
    val provider =  ListProvider(this.sourceType, this.receiverType)
    provider.add(this)
    return provider
}


inline fun <reified T, reified V> KProperty1<T, V>.toElementProvider(): ElementProvider<T, V>
        = ElementProvider<T, V>(this)

inline fun <T, reified V> KProperty1<T, V>.toElementProvider(receiverType: TypeToken<T>): ElementProvider<T, V>
        = ElementProvider<T, V>(receiverType, this)

inline fun <reified T, reified V> Function1<T, V>.toElementProvider(): ElementProvider<T, V>
        = ElementProvider<T, V>(this)

fun <T, V> Function1<T, V>.toElementProvider(receiverType: TypeToken<T>, valueType: TypeToken<V>): ElementProvider<T, V>
        = ElementProvider<T, V>(receiverType, valueType, this)

fun <T, V> ElementProvider<T, V>.createLoader(name: String): DataLoader<T, V>{
    return  DataLoader(name, sourceType, receiverType).apply(this)
}

fun <T, V> ReceiverCallable<T, V>.createProvider(): ElementProvider<T, V>{
    val provider =  ElementProvider(this.sourceType, this.receiverType)
    provider.add(this)
    return provider
}


