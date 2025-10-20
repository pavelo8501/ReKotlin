package po.misc.types.token

import po.misc.types.TypeProvider


interface Tokenized<T: Any> : TokenHolder, TokenFactory{
    override val typeToken: TypeToken<T>
}

interface TokenHolder: TypeProvider{
    val typeToken: TypeToken<*>
    override val types: List<TypeToken<*>> get() = listOf(typeToken)
}
