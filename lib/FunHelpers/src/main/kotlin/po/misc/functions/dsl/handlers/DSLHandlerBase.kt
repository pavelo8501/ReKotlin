package po.misc.functions.dsl.handlers

import po.misc.functions.dsl.DSLConstructor


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