package po.misc.collections.lambda_list

import po.misc.context.component.Component
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.functions.CallableOptions
import po.misc.functions.LambdaOptions
import po.misc.types.k_function.lambdaName
import po.misc.types.token.TypeToken


interface LambdaWrapper<H, T>: Component  {
    val lambdaName: String
    val options: CallableOptions
    val isSuspended: Boolean
    fun apply(receiver: H, value: T)
}


class LambdaConfigurator<H: Any, T>(
    val parameterType: TypeToken<T>,
    override val options: LambdaOptions = LambdaOptions.Listen,
    private val lambda: H.(T) -> Unit
):LambdaWrapper<H, T> {

    override val isSuspended: Boolean = false
    override val lambdaName: String = options.name?: lambda::class.lambdaName

    override val componentID: ComponentID = componentID({ "Lambda $lambdaName" })

    constructor(
        parameterType: TypeToken<T>,
        lambda: H.(T) -> Unit
    ) : this(parameterType, LambdaOptions.Listen, lambda)

    override fun apply(receiver: H, value: T){
        lambda.invoke(receiver, value)
    }
}