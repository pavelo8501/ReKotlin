package po.misc.types


import po.misc.types.token.TypeToken

interface TypeProvider{
    val types: List<TypeToken<*>>
}

interface DoubleTyped<T1: Any, T2: Any>{
    val parameter1: TypeToken<T1>
    val parameter2: TypeToken<T2>

    val types: List<TypeToken<*>>
        get() = listOf(parameter1, parameter2)
}








