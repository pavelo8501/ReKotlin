package po.misc.callbacks

import po.misc.collections.lambda_list.LambdaConfigurator
import po.misc.functions.LambdaOptions
import po.misc.types.token.TypeToken

interface FunctionalHelper {


    fun <H: Any> Function2<H, Unit, Unit>.toConfigurator(
        optionalName: String? = null
    ):LambdaConfigurator<H, Unit>{
        val unitToken = TypeToken.create<Unit>()
       return optionalName?.let {
            LambdaConfigurator(unitToken, LambdaOptions.Promise.applyName(it), this)
        }?:run {
            LambdaConfigurator(unitToken, LambdaOptions.Promise, this)
        }
    }

    fun <H: Any, T> Function2<H, T, Unit>.toConfigurator(
        parameterType: TypeToken<T>,
        optionalName: String? = null,
    ):LambdaConfigurator<H, T>{
        return optionalName?.let {
            LambdaConfigurator(parameterType, LambdaOptions.Promise.applyName(it), this)
        }?:run {
            LambdaConfigurator(parameterType, LambdaOptions.Promise, this)
        }
    }

}