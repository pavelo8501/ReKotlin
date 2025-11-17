package po.misc.dsl.configurator

import po.misc.context.component.configSubject
import po.misc.context.component.initSubject
import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import po.misc.data.logging.LogProvider
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.logProcessor
import po.misc.reflection.primitives.NullClass
import po.misc.types.helpers.filterTokenized
import po.misc.types.helpers.safeCast
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.safeCast
import po.misc.types.token.TypeToken
import kotlin.enums.enumEntries
import kotlin.reflect.KClass


sealed interface DSLConfiguratorBuilder<T: TraceableContext> {


    fun addGroup(priority: HasNameValue, block:DSLGroup<T>.()->  DSLGroup<T>)

    fun <P> createParametrizedGroup(
        parameterToken:  TypeToken<P>,
        priority: HasNameValue,
    ):  DSLParameterGroup<T, P>
}


inline fun <T: TraceableContext, reified P> DSLConfiguratorBuilder<T>.addParametrizedGroup(
    priority: HasNameValue,
    block: DSLParameterGroup<T, P>.()-> DSLParameterGroup<T, P>
){
    val group = createParametrizedGroup<P>(TypeToken.create(), priority)
    block.invoke(group)
}


class DSLConfigurator<T: TraceableContext>(
    groups: List<DSLGroup<T>>
): DSLConfiguratorBuilder<T>, LogProvider<LogMessage> {

    constructor(vararg priorities: HasNameValue): this( priorities.map { DSLGroup<T>(it) })

    internal val unprioritized = DSLGroup<T>(Unprioritized)
    @PublishedApi
    internal val prioritized: MutableMap<Int, DSLGroups<T, *>> = mutableMapOf()
    internal val logger = logProcessor()

    val size: Int get() =  prioritized.values.sumOf { it.configurators.size } + unprioritized.configurators.size
    val groupCount: Int get() =  prioritized.size

    init {
        groups.toList().forEach {

           prioritized[it.priority.value] = it
        }
    }

    fun groupSize(priority: HasNameValue): Int{
        return prioritized[priority.value]?.configurators?.size?:0
    }

    fun addConfigurator(
        priority: HasNameValue,
        block:T.()-> Unit
    ){
        prioritized[priority.value]?.let {
            if(it is DSLGroup){
                it.addConfigurator(block =  { block.invoke(this) })
            }else{
                val wrongDSL = "Wrong configuration provided. Expecting parametrized DSL for priority ${priority.name} # ${priority.value}"
                warn(initSubject, wrongDSL)
            }
        }?:run {
            unprioritized.addConfigurator { block.invoke(this)  }
        }
    }

    @PublishedApi
    internal val wrongParameter : HasNameValue.(KClass<*>, TypeToken<*>) -> String = {received, expected ->
        "Wrong configuration provided." +
                "DSLGroup is expecting parameter of type ${expected.typeName} DSL for priority ${name} # ${value}" +
                "Got ${received.simpleOrAnon}"
    }


    fun createGroup(
        priority: HasNameValue,
    ):  DSLGroup<T>{
        val group = DSLGroup<T>(priority)
        prioritized[priority.value] = group
        return group
    }

    @JvmName("addConfiguratorWithParameter")
    inline fun <reified P: Any> addConfigurator(
        priority: HasNameValue,
        noinline block:T.(P)-> Unit
    ){
        prioritized[priority.value]?.let {
            val casted = it.safeCast<DSLParameterGroup<T, P>, P>(P::class)
            casted?.addConfigurator(block = block) ?: warn(initSubject, wrongParameter(priority, P::class, it.parameterType))
        }?:run {
            val group = createParametrizedGroup<P>(priority)
            group.addConfigurator(block =  block)
        }
    }

   override fun addGroup(
        priority: HasNameValue,
        block: DSLGroup<T>.()-> DSLGroup<T>
    ){
        val group =  createGroup(priority)
        block.invoke(group)
    }

    override fun <P> createParametrizedGroup(
        parameterToken:  TypeToken<P>,
        priority: HasNameValue,
    ):  DSLParameterGroup<T, P>{
        val group = DSLParameterGroup<T, P>(priority, parameterToken)
        prioritized[priority.value] = group
        return group
    }

    inline fun <reified P> createParametrizedGroup(
        priority: HasNameValue,
    ):  DSLParameterGroup<T, P> = createParametrizedGroup(TypeToken.create<P>(), priority)

    @JvmName("addGroupWithParameter")
    inline fun <reified P: Any> addParametrizedGroup(
        priority: HasNameValue,
        block: DSLParameterGroup<T, P>.()-> DSLParameterGroup<T, P>
    ){
        val group = createParametrizedGroup<P>(priority)
        block.invoke(group)
    }


    fun sequence(groupProvider: (DSLGroup<T>)-> Unit){
        val sorted = prioritized.entries.sortedBy { it.key }
        val filtered = sorted.mapNotNull { it.value.safeCast<DSLGroup<T>>() }
        filtered.forEach {  value->
            groupProvider.invoke(value)
        }
    }

    fun applyConfig(receiver: T):T{
        val sorted = prioritized.entries.sortedBy { it.key }
        val filtered = sorted.mapNotNull { it.value.safeCast<DSLGroup<T>>() }
        filtered.forEach {  value->
            value.apply(receiver, Unit)
        }
        unprioritized.apply(receiver, Unit)
        return receiver
    }

    fun applyConfig(receiver: T, priority: HasNameValue): Boolean{
        prioritized[priority.value]?.let {
            val casted = it.safeCast<DSLGroup<T>>()
            if(casted != null){
                casted.apply(receiver, Unit)
                return true
            }else{
                warn(configSubject,  wrongParameter(priority, Unit::class, it.parameterType))
                return false
            }
        }?:run {
            warn(configSubject, "No DSLGroup found for priority ${priority.name} # ${priority.value}")
        }
        return false
    }

    fun <P: Any> applyConfig(receiver: T, parameter:P): T{
        val sorted = prioritized.values.filterIsInstance<DSLParameterGroup<T, P>>().sortedBy { it.priority.value }
        sorted.forEach {
            it.apply(receiver, parameter)
        }
        return receiver
    }

    fun <P> applyConfig(receiver: T, parameter:P, priority: HasNameValue): Boolean{
        prioritized[priority.value]?.let {
            val casted = it.safeCast<DSLParameterGroup<T, P>>()
            if(casted != null){
                casted.apply(receiver, parameter)
                return true
            }else{
                if(parameter != null){
                    warn(configSubject,  wrongParameter(priority, parameter::class, it.parameterType))
                }else{
                    warn(configSubject,  wrongParameter(priority, NullClass::class, it.parameterType))
                }

                return false
            }
        }?:run {
            warn(configSubject, "No DSLParametrizedGroup found for priority ${priority.name} # ${priority.value}")
        }
        return false
    }

    @JvmName("sequenceDSLParameterGroup")
    inline fun <reified P: Any> sequence(groupProvider: (DSLParameterGroup<T, P>)-> Unit){
        val sorted = prioritized.values.filterTokenized<DSLParameterGroup<T, P>, P>(P::class).sortedBy { it.priority.value }
        sorted.forEach {  value->
            groupProvider.invoke(value)
        }
    }

    companion object {

        inline operator fun <T : TraceableContext,  reified E> invoke(): DSLConfigurator<T> where E : HasNameValue, E : Enum<E> {
            val groups = enumEntries<E>().map {
                DSLGroup<T>(it)
            }
            return DSLConfigurator(groups)
        }
    }
}