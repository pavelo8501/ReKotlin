package po.misc.containers.lazy

import po.misc.containers.BackingBuilder
import po.misc.context.tracable.TraceableContext
import po.misc.types.token.TypeToken

inline fun <reified T: Any> lazyContainerOf():LazyContainer<T>{
    return LazyContainer()
}

fun <T: Any> lazyContainerOf(
    typeToken: TypeToken<T>
):LazyContainer<T>{
    return LazyContainer(typeToken)
}

inline fun <reified T: Any> lazyContainer(
    builder: BackingBuilder<T>.()-> Unit
):LazyContainer<T>{
    val container = LazyContainer<T>()
    container.builder()
    return container
}

fun <T: Any> TraceableContext.lazyContainer(
    typeToken: TypeToken<T>,
    builder: BackingBuilder<T>.()-> Unit
):LazyContainer<T>{
    val container = LazyContainer(typeToken)
    container.builder()
    return container
}