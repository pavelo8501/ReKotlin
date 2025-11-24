package po.misc.types

import po.misc.types.token.TokenFactory
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import po.misc.types.token.tokenOf

class TokenizedList<T: Any>(
    val typeToken: TypeToken<T>,
    val onEvery: ((T)-> Unit)? = null
): AbstractMutableList<T>(){

    class TokenizedContainer<T: Any>(
        val receiver:T,
        override val typeToken: TypeToken<T>
    ): Tokenized<T>

    private val listBacking: MutableList<TokenizedContainer<T>> = mutableListOf()
    val tokenized: List<TokenizedContainer<T>> get() = listBacking

    override val size: Int get() = listBacking.size

    private fun pack(element: T):TokenizedContainer<T>{
       return TokenizedContainer(element, typeToken)
    }
    private fun TokenizedContainer<T>.unPack():T{
        return receiver
    }

    override fun get(index: Int): T {
        return listBacking[index].unPack()
    }
    override fun set(index: Int, element: T): T {
        listBacking[index] = pack(element)
        onEvery?.invoke(element)
        return element
    }
    override fun removeAt(index: Int): T {
        return  listBacking.removeAt(index).unPack()
    }
    override fun add(index: Int, element: T) {
        listBacking.add(index, pack(element))
        onEvery?.invoke(element)
    }

    companion object: TokenFactory{
       inline operator fun <reified T: Any> invoke(
          noinline onEvery: ((T)-> Unit)? = null
       ):TokenizedList<T>{
          return TokenizedList(tokenOf<T>(), onEvery)
       }
    }
}

inline fun <reified T: Any> tokenizedListOf(
   noinline onEvery: ((T)-> Unit)? = null
): TokenizedList<T>{
    return TokenizedList(TypeToken.create<T>(), onEvery)
}