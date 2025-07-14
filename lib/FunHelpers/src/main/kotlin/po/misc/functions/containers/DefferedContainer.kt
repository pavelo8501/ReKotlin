package po.misc.functions.containers

import po.misc.functions.hooks.ReactiveComponent
import po.misc.interfaces.CtxId
import po.misc.types.getOrManaged




//
//sealed class FunContainer<T: Any>(
//    val ctx: CtxId
//): ReactiveComponent<T>{
//
//   protected var valueBacking: T? = null
//   abstract override val isLambdaProvided: Boolean
//   override var isResolved: Boolean = false
//   override val hooks: ReactiveHooks<T> = ReactiveHooks<T>()
//
//    val value:T get() {
//        val obtainedResult =  invokeLambda()
//        valueResolved(obtainedResult)
//        return obtainedResult
//    }
//
//    override val currentValue: T? = valueBacking
//
//    abstract fun invokeLambda():T
//
//    protected fun valueResolved(value:T){
//        isResolved = true
//        valueBacking = value
//        hooks.fireResolve(value)
//    }
//
//    fun onResolve(block: (T) -> Unit): FunContainer<T> {
//        hooks.onResolve(block)
//        valueBacking = null
//        return this
//    }
//    override fun dispose() {
//        hooks.fireDispose()
//    }
//}
//
//
//class DeferredResultContainer<T: Any>(
//    ctx: CtxId,
//    var resultLambda:(()-> T)?,
//): FunContainer<T>(ctx) {
//
//    override val isLambdaProvided: Boolean  get() = resultLambda != null
//    override fun invokeLambda():T {
//        val lambda = resultLambda.getOrManaged("ResultLambda", ctx)
//        return lambda.invoke()
//    }
//
//    fun setResultProvider(resultProvider: ()-> T){
//        resultLambda = resultProvider
//        hooks.fireInit()
//        isResolved = false
//    }
//
//    companion object{
//        fun <T: Any>  create(creator: CtxId,  resultLambda: (()->T)? = null):DeferredResultContainer<T>{
//            return  DeferredResultContainer(creator, resultLambda)
//        }
//    }
//}
