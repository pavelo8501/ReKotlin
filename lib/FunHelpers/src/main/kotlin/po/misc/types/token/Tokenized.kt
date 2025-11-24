package po.misc.types.token


interface TypeProvider{
    val types: List<TypeToken<*>>
}

interface Tokenized<T> : TokenHolder, TokenFactory{
    override val typeToken: TypeToken<T>
}

interface TokenHolder: TypeProvider{
    val typeToken: TypeToken<*>
    override val types: List<TypeToken<*>> get() = listOf(typeToken)
}
