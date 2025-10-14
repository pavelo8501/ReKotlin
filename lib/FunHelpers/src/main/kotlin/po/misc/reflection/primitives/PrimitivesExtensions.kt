package po.misc.reflection.primitives


inline fun <reified T: Any> PrimitiveClass<T>.of(): PrimitiveClass<T>?{
    @Suppress("UNCHECKED_CAST")
    return  PrimitiveClass.ofClass(T::class) as? PrimitiveClass<T>
}