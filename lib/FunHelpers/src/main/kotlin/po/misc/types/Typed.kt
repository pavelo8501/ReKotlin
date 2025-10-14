package po.misc.types

import po.misc.types.type_data.TypeData
import po.misc.types.type_data.TypeDataCommon
import po.misc.types.token.TypeToken


sealed interface TypeProvider{
    val types: List<TypeToken<*>>
}

@Deprecated("Switch to Tokenized")
interface Typed<T: Any>{
    val typeData: TypeData<T>
    val types: List<TypeDataCommon<T>> get() = listOf(typeData)
}



interface TokenHolder: TypeProvider{
    val typeToken: TypeToken<*>
    override val types: List<TypeToken<*>> get() = listOf(typeToken)
}

interface Tokenized<T: Any> : TokenHolder{
    override val typeToken: TypeToken<T>
}

interface DoubleTyped<T1: Any, T2: Any>{
    val parameter1: TypeDataCommon<T1>
    val parameter2: TypeDataCommon<T2>

    val types: List<TypeDataCommon<*>>
        get() = listOf(parameter1, parameter2)
}








