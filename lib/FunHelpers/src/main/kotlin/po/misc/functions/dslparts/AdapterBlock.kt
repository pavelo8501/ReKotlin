package po.misc.functions.dslparts


data class AdapterBlock<T: Any, R: Any>(
    private val lambda:(R)-> T
):Invocable<R, T>{
    override fun execute(receiver:R):T{
        return lambda.invoke(receiver)
    }
}

data class LambdaBlock<T: Any, R: Any>(
    val lambdaAdapter:AdapterBlock<R, T>,
    private val lambda:(T)-> Unit
):Invocable<T, R>{

    val result:(T) -> R = {value->
        lambdaAdapter.execute(value)
    }
    override fun execute(receiver:T):R{
        return result(receiver)
    }
}

data class AdapterBlock2<R: Any, T: Any>(
    private val lambda: (T) -> R
): Invocable<T, R> {
    override fun execute(receiver: T): R = lambda(receiver)
}

data class LambdaBlock2<T: Any, R: Any>(
    val lambdaAdapter: AdapterBlock2<R, T>,
    private val lambda: (R) -> T
): Invocable<T, R> {

    override fun execute(receiver: T): R {
        val value = lambdaAdapter.execute(receiver)
        lambda(value)
        return lambdaAdapter.execute(receiver)
    }
}


