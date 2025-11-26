package po.misc.containers.backing

import po.misc.containers.BackingBuilder
import po.misc.context.tracable.TraceableContext
import po.misc.types.token.TypeToken



fun <T: Any> backingContainerOf(
    typeData: TypeToken<T>
):BackingContainer<T>{
    return BackingContainer(typeData)
}

inline fun <reified T: Any> backingContainerOf():BackingContainer<T> =
    backingContainerOf(TypeToken.create<T>())



inline fun <reified T: Any> backingContainer(
    builder: BackingBuilder<T>.()-> Unit
):BackingContainer<T>{
    val container = BackingContainer<T>()
    container.builder()
    return container
}

fun <T: Any> backingContainer(
    typeToken: TypeToken<T>,
    builder: BackingBuilder<T>.()-> Unit
):BackingContainer<T>{
    val container = BackingContainer(typeToken)
    container.builder()
    return container
}