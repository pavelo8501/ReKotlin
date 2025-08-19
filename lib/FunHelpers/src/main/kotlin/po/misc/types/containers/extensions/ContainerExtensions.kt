package po.misc.types.containers.extensions


import po.misc.types.containers.TypedContainer

fun <T: Any> TypedContainer<T>.isResultNullable(): Boolean {
    val traits = classInfo.traits
    return traits.isUnit || traits.isNullable
}