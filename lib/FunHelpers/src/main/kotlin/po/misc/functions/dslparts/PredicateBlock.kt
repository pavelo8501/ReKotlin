package po.misc.functions.dslparts


interface Invocable<T: Any,  R: Any?>{
    fun execute(receiver:T): R
}

data class ConditionalBlock<T: Any>(
   private val predicate:(T)-> Boolean
):Invocable<T, Boolean>{
    val result: (T) -> Boolean = predicate
    fun evaluate(receiver:T): Boolean{
      return result(receiver)
    }
    override fun execute(receiver:T): Boolean = evaluate(receiver)
}
