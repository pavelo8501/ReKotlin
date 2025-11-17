package po.misc.dsl.configurator

import po.misc.context.component.configSubject
import po.misc.context.component.initSubject
import po.misc.context.component.startProcSubject
import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import po.misc.data.logging.LogProvider
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.logProcessor
import po.misc.reflection.primitives.NullClass
import po.misc.types.helpers.filterTokenized
import po.misc.types.helpers.safeCast
import po.misc.types.helpers.simpleOrAnon
import po.misc.types.safeCast
import po.misc.types.token.TokenFactory
import po.misc.types.token.TypeToken
import kotlin.enums.enumEntries
import kotlin.reflect.KClass


/**
 * A configurator that manages DSL groups with priority-based execution order.
 *
 * @param T The type of traceable context that configurations will be applied to
 * @property dslGroups Mutable map of priority values to DSL configuration groups
 * @property size Total number of configurators across all groups
 * @property groupCount Number of priority groups in the configurator
 *
 */
class DSLConfigurator<T: TraceableContext>(
    groups: Collection<DSLConfigurable<T, *>>
): LogProvider<LogMessage>, TokenFactory {

    constructor(vararg priorities: HasNameValue) : this(priorities.map { DSLGroup<T>(it) })

    val logger: LogProcessor<DSLConfigurator<T>, LogMessage> = logProcessor()

    private val subjectApplyConfig = "Apply config"

    internal val unprioritized = DSLGroup<T>(Unprioritized)

    @PublishedApi
    internal val dslGroups: MutableMap<Int, DSLConfigurable<T, *>> = mutableMapOf()


    @PublishedApi
    internal val wrongParameter: HasNameValue.(KClass<*>, TypeToken<*>) -> String = { received, expected ->
        "Wrong configuration provided." +
                "DSLGroup is expecting parameter of type ${expected.typeName} DSL for priority $name # $value" +
                "Got ${received.simpleOrAnon}"
    }

    val size: Int get() = dslGroups.values.sumOf { it.configurators.size } + unprioritized.configurators.size
    val groupCount: Int get() = dslGroups.size

    init {
        groups.toList().forEach {
            dslGroups[it.priority.value] = it
        }
    }

    private fun onStart(receiver: T, group: DSLConfigurable<T, *>) {
        group.trigger(ProgressTracker(receiver, group), State.Initialized)
    }
    private fun onComplete(receiver: T, group: DSLConfigurable<T, *>) {
        group.trigger(ProgressTracker(receiver, group), State.Complete)
    }

    fun groupSize(priority: HasNameValue): Int {
        return dslGroups[priority.value]?.configurators?.size ?: 0
    }

    fun addConfigurator(
        priority: HasNameValue,
        block: T.() -> Unit
    ) {
        dslGroups[priority.value]?.let {
            if (it is DSLGroup) {
                it.addConfigurator(block = { block.invoke(this) })
            } else {
                val wrongDSL =
                    "Wrong configuration provided. Expecting parametrized DSL for priority ${priority.name} # ${priority.value}"
                warn(initSubject, wrongDSL)
            }
        } ?: run {
            unprioritized.addConfigurator { block.invoke(this) }
        }
    }

    fun createGroup(priority: HasNameValue): DSLGroup<T> {
        val group = DSLGroup<T>(priority)
        dslGroups[priority.value] = group
        return group
    }

    fun <P> createGroup(
        parameterType: TypeToken<P>,
        priority: HasNameValue,
    ): DSLParameterGroup<T, P> {
        val group = DSLParameterGroup<T, P>(priority, parameterType)
        dslGroups[priority.value] = group
        return group
    }

    /**
     * Creates and configures a parameterless DSL group using the provided block.
     *
     * @param priority The priority for the new group
     * @param block Configuration block to set up the DSL group
     * @return The configured DSLGroup instance
     *
     */
    fun buildGroup(priority: HasNameValue, block: DSLGroup<T>.() -> Unit): DSLGroup<T> {
        val group = createGroup(priority)
        block.invoke(group)
        return group
    }

    /**
     * Creates and configures a parameterized DSL group using reified type parameters.
     *
     * @param P The reified type of parameter expected by the configurators
     * @param priority The priority for the new group
     * @param block Configuration block to set up the parameterized DSL group
     * @return The configured DSLParameterGroup instance
     */
    fun <P> buildGroup(
        parameterType: TypeToken<P>,
        priority: HasNameValue,
        block: DSLParameterGroup<T, P>.() -> Unit
    ): DSLParameterGroup<T, P> {
        val group = createGroup(parameterType, priority)
        group.block()
        return group
    }

    /**
     * Creates and configures a parameterized DSL group using reified type parameters.
     *
     * @param P The reified type of parameter expected by the configurators
     * @param priority The priority for the new group
     * @param block Configuration block to set up the parameterized DSL group
     * @return The configured DSLParameterGroup instance
     */
    inline fun <reified P> buildGroup(
        priority: HasNameValue,
        noinline block: DSLParameterGroup<T, P>.() -> Unit
    ): DSLParameterGroup<T, P> = buildGroup(TypeToken.create<P>(), priority, block)

    /**
     * Builds a strongly-typed [DSLParameterGroup] using an explicit reference to
     * [DSLParameterGroup.Companion] to disambiguate overloads in complex generic cases.
     *
     * This overload exists for scenarios where the compiler cannot reliably infer the
     * correct `P` type or selects the wrong `buildGroup` overload due to signature
     * similarity, especially when:
     *  - The caller uses reified generic parameters.
     *  - Multiple `buildGroup` overloads accept the same number of parameters.
     *  - Type inference collides with `TypeToken<P>`–based overloads.
     *
     * By requiring an explicit `DSLParameterGroup.Companion` receiver, the caller
     * clearly indicates that they intend to construct a parameter-based DSL group,
     * forcing the compiler to resolve the correct function without ambiguity.
     *
     * Example:
     * ```
     * buildGroup<String?>(DSLParameterGroup, ConfigPriority.Top) {
     *     addConfigurator("nullable_config") { value ->
     *         // ...
     *     }
     * }
     * ```
     *
     * @param dslParameterGroup A companion object marker used solely to enforce
     *                          selection of this overload and eliminate ambiguity.
     * @param priority The priority identifier for this group, typically an enum
     *                 implementing [HasNameValue].
     * @param block The configuration DSL applied to the created [DSLParameterGroup].
     *
     * @return The newly created [DSLParameterGroup] registered inside the configurator.
     */
    inline fun <reified P> buildGroup(
        dslParameterGroup: DSLParameterGroup.Companion,
        priority: HasNameValue,
        noinline block: DSLParameterGroup<T, P>.() -> Unit
    ): DSLParameterGroup<T, P> = buildGroup(TypeToken.create<P>(), priority, block)

    internal fun <P> runGroupConfig(receiver: T, parameter: P, group: DSLConfigurable<T, *>) {
        onStart(receiver, group)
        when (group) {
            is DSLGroup -> {
                group.applyConfig(receiver, Unit)
            }

            is DSLParameterGroup -> {
                if (parameter != null) {
                    val casted = group.safeCast<DSLConfigurable<T, P>>()
                    if (casted != null) {
                        casted.applyConfig(receiver, parameter)
                    } else {
                        warn(configSubject, wrongParameter(group.priority, parameter::class, group.parameterType))
                    }
                } else {
                    group.safeCast<DSLConfigurable<T, Any?>>()?.let {
                        it.applyConfig(receiver, null)
                        infoMsg(subjectApplyConfig, "Applying null as a parameter value")
                    } ?: run {
                        warn(subjectApplyConfig, "$group configuration skipp")
                    }
                }
            }
        }
        onComplete(receiver, group)
    }

    internal fun <P> runGroupConfig(receiver: T, parameter: P, priority: HasNameValue) {
        val exists3 = dslGroups[priority.value]
        if (exists3 != null) {
            runGroupConfig(receiver, parameter, exists3)
        } else {
            warn(subjectApplyConfig, "No DSL group found for name  ${priority.name}")
        }
    }

    fun applyConfig(receiver: T): Boolean {
        val sorted = dslGroups.entries.sortedBy { it.key }
        val filtered = sorted.mapNotNull { it.value.safeCast<DSLGroup<T>>() }
        try {
            filtered.forEach { value ->
                value.applyConfig(receiver, Unit)
            }
            unprioritized.applyConfig(receiver, Unit)
            return true
        } catch (th: Throwable) {
            warn("applyConfig", th)
            return false
        }
    }
    fun applyConfig(receiver: T, priority: HasNameValue): Boolean {
        runGroupConfig(receiver, Unit, priority)
        return false
    }

    fun <P> applyConfig(receiver: T, parameter: P): Boolean {
        val sorted = dslGroups.entries.sortedBy { it.key }.map { it.value }
        sorted.forEach {
            runGroupConfig(receiver, parameter, it)
        }
        return true
    }
    fun <P> applyConfig(receiver: T, parameter: P, priority: HasNameValue): Boolean {
        runGroupConfig(receiver, parameter, priority)
        return false
    }

    companion object {


        inline operator fun <T : TraceableContext, reified E> invoke(): DSLConfigurator<T> where E : HasNameValue, E : Enum<E> {
            val groups = enumEntries<E>().map {
                DSLGroup<T>(it)
            }
            return DSLConfigurator(groups)
        }
    }
}