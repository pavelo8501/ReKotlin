package po.misc.functions.dsl.handlers

import po.misc.functions.dsl.DSLConstructor
import po.misc.types.token.TypeToken

abstract class DSLHandlerBase<T: Any, R: Any>(
    private val constructor: DSLConstructor<T, R>
) {

    var resultModification: ((R)-> R)? = null

    fun applyToResult(defaultAction: (R)-> R) {
        resultModification = defaultAction
    }

    internal fun transformResult(result:R):R{
       return resultModification?.invoke(result)?:result
    }
}

class DSLHandler<T: Any, R: Any>(
    constructor: DSLConstructor<T, R>
): DSLHandlerBase<T, R>(constructor)



abstract class HandlerBase<T: Any>(
  val typeData: TypeToken<T>
) {
    protected val modifications: MutableList<(T)-> T> = mutableListOf()

    fun applyToResult(modification: (T)-> T) {
        modifications.add(modification)
    }

    fun trigger(result:T):T{
        var resultingModification:T? = null
        modifications.forEach {
            resultingModification =  it.invoke(result)
        }
        modifications.clear()
        return resultingModification?:result
    }
}

class DefaultDSLHandler<T: Any>(
    typeData: TypeToken<T>
):HandlerBase<T>(typeData){

}