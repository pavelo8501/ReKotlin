package po.misc.dsl.configurator

import po.misc.callbacks.FunctionalHelper
import po.misc.collections.lambda_list.LambdaWrapper
import po.misc.data.HasNameValue
import po.misc.data.HasValue
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken


internal object Unprioritized: HasNameValue{
    override val name: String = "Unprioritized"
    override val value: Int = 0
}


sealed interface DSLGroups<T: Any, P>: Tokenized<P>{

    val priority: HasNameValue
    val parameterType: TypeToken<P>
    val configurators :  List<LambdaWrapper<T, P>>
    fun apply(receiver: T, parameter:P):T
}

class DSLGroup<T: Any>(
    override val priority: HasNameValue,
): DSLGroups<T, Unit>, FunctionalHelper {

    override val parameterType: TypeToken<Unit> = TypeToken.create()
    override val typeToken: TypeToken<Unit> get() = parameterType
    val groupName: String = priority.name
    override val configurators : MutableList<LambdaWrapper<T, Unit>> = mutableListOf()
    fun addConfigurator(optionalName: String? = null,  block: T.(Unit)-> Unit):DSLGroup<T>{
        val configurator = block.toConfigurator<T>(optionalName)
        configurators.add(configurator)
        return this
    }

    override fun apply(receiver: T, parameter: Unit):T{
        configurators.forEach {
            it.apply(receiver, Unit)
        }
        return receiver
    }

    fun applyConfig(receiver: T) = apply(receiver, Unit)

}

class DSLParameterGroup<T: Any, P>(
    override val priority: HasNameValue,
    override val parameterType: TypeToken<P>
): DSLGroups<T, P>, FunctionalHelper {
    override val typeToken: TypeToken<P> get() = parameterType
    val groupName: String = priority.name
    override val configurators : MutableList<LambdaWrapper<T, P>> = mutableListOf()

    fun addConfigurator(optionalName: String? = null,  block: T.(P)-> Unit):DSLParameterGroup<T, P>{
        val configurator = block.toConfigurator<T, P>(typeToken,  optionalName)
        configurators.add(configurator)
        return this
    }
    override fun apply(receiver: T, parameter:P):T{
        configurators.forEach {
            it.apply(receiver, parameter)
        }
        return receiver
    }
}