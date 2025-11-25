package po.misc.dsl.configurator

import po.misc.callbacks.signal.signalOf
import po.misc.context.component.ComponentID
import po.misc.context.component.componentID
import po.misc.context.component.configSubject
import po.misc.context.component.initSubject
import po.misc.context.log_provider.LogProvider
import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import po.misc.data.logging.log_subject.startProcSubject
import po.misc.data.logging.models.LogMessage
import po.misc.data.logging.processor.LogProcessor
import po.misc.data.logging.processor.createLogProcessor
import po.misc.debugging.ClassResolver
import po.misc.dsl.configurator.data.ConfigurationTracker
import po.misc.dsl.configurator.data.ConfiguratorInfo
import po.misc.functions.Throwing
import po.misc.types.k_class.simpleOrAnon
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
): LogProvider,  TokenFactory {

    constructor(vararg priorities: HasNameValue) : this(priorities.map { DSLGroup<T>(it) })

    override val componentID: ComponentID = componentID()

    override val logProcessor: LogProcessor<DSLConfigurator<T>, LogMessage> = createLogProcessor()

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

    val startConfiguration = startProcSubject("Start configuration")

    internal val configurationStart = signalOf<ConfiguratorInfo<T>, Unit>()
    internal val configurationComplete = signalOf<ConfiguratorInfo<T>, Unit>()
    internal val configurationStep = signalOf<ConfigurationTracker<T>, Unit>()


    internal val configMessage: (DSLConfigurable<T, *>, T) -> String = { group, receiver ->
        "Applying configuration group ${group.groupName} to ${ClassResolver.resolveInstance(receiver).instanceName}"
    }

    init {
        groups.toList().forEach {
            dslGroups[it.priority.value] = it
        }
    }

    fun onConfigurationStart(callback: ConfiguratorInfo<T>.() -> Unit){
        configurationStart.onSignal(callback)
    }

    fun onConfigurationComplete(callback: ConfiguratorInfo<T>.() -> Unit){
        configurationComplete.onSignal(callback)
    }

    fun onConfiguration(callback: (ConfigurationTracker<T>) -> Unit){
        configurationStep.onSignal(callback)
    }

    fun groupSize(priority: HasNameValue): Int {
        return dslGroups[priority.value]?.configurators?.size ?: 0
    }

    fun addConfigurator(priority: HasNameValue, block: T.() -> Unit) {
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

    internal fun <P> runGroupConfig(
        configurator: DSLConfigurator<T>,
        receiver: T,
        parameter: P,
        group: DSLConfigurable<T, *>,
        singleLaunch: Boolean
    ) {
        val info = ConfiguratorInfo(this, 1, group.configurators.size )
        if(singleLaunch){
            configurationStart.trigger(info)
        }
        when (group) {
            is DSLGroup -> {
                group.applyConfig(configurator, receiver, Unit)
            }
            is DSLParameterGroup -> {
                if (parameter != null) {
                    val casted = group.safeCast<DSLConfigurable<T, P>>()
                    if (casted != null) {
                        casted.applyConfig(configurator, receiver, parameter)
                    } else {
                        warn(configSubject, wrongParameter(group.priority, parameter::class, group.parameterType))
                    }
                } else {
                    group.safeCast<DSLConfigurable<T, Any?>>()?.let {
                        it.applyConfig(configurator, receiver, null)
                        infoMsg(subjectApplyConfig, "Applying null as a parameter value")
                    } ?: run {
                        warn(subjectApplyConfig, "$group configuration skipp")
                    }
                }
            }
        }
        if(singleLaunch){
            info.finalizeResult(1)
            configurationComplete.trigger(info)
        }
    }

    internal fun <P> runGroupConfig(
        receiver: T,
        parameter: P,
        priority: HasNameValue,
        singleLaunch: Boolean
    ): Throwable? {
        try {
            val exists = dslGroups[priority.value]
            if (exists != null) {
                runGroupConfig(this,  receiver, parameter, exists, singleLaunch)
            } else {
                warn(subjectApplyConfig, "No DSL group found for name  ${priority.name}")
            }
            return null
        } catch (th: Throwable) {
            warn("applyConfig", th)
            return th
        }
    }

    /**
     * Applies configuration to the given [receiver] only for the DSL group that
     * matches the specified [priority].
     *
     * The underlying configuration block is executed using `runGroupConfig`.
     * If the DSL group executes without throwing an exception, this method returns `true`.
     *
     * If the DSL group throws an exception internally, it is *not* rethrown here.
     * Instead, the method returns `false` to indicate a failure.
     *
     * @param receiver The target object that receives the configuration.
     * @param priority The name/value pair identifying the DSL group to execute.
     *
     * @return `true` if configuration completed successfully, or `false` if the
     *         priority-group configuration produced an exception.
     */
    fun applyConfig(receiver: T, priority: HasNameValue): Boolean {
        runGroupConfig(receiver, Unit, priority, singleLaunch = true) ?: return true
        return false
    }

    /**
     * Applies configuration to the given [receiver] for the DSL group matching
     * the specified [priority], rethrowing any exception that occurs inside the
     * configuration block.
     *
     * Equivalent to [applyConfig] but enforces strict error propagation:
     * if `runGroupConfig` returns a non-null [Throwable], it is thrown immediately.
     *
     * @param receiver The target object to configure.
     * @param priority The DSL group to execute.
     * @param throwing Marker parameter that enables exception propagation.
     *
     * @throws Throwable If the priority-based configuration fails.
     */
    fun applyConfig(receiver: T, priority: HasNameValue, throwing: Throwing) {
        runGroupConfig(receiver, Unit, priority, singleLaunch = true)?.let {
            throw it
        }
    }

    /**
     * Applies configuration across all registered DSL groups for the provided [receiver],
     * in order of ascending priority. Each group is executed via `runGroupConfig`.
     *
     * Exceptions produced by individual groups are collected but not rethrown.
     * If one or more groups fail, the method returns `false`.
     *
     * After all prioritized DSL groups are executed, the unprioritized fallback group
     * (`unprioritized`) is applied.
     *
     * @param receiver The target object to configure.
     *
     * @return `true` if all prioritized DSL groups succeeded, or `false` if any group
     *         produced an exception during execution.
     */
    fun applyConfig(receiver: T): Boolean {
        val throwList = mutableListOf<Throwable>()
        val sorted = dslGroups.entries.sortedBy { it.key }.map { it.value }

        val info = ConfiguratorInfo(this, sorted.size, sorted.sumOf { it.configurators.size })
        configurationStart.trigger(info)

        sorted.forEach {
            val thrown = runGroupConfig(receiver, Unit, it.priority, singleLaunch = false)
            if (thrown != null) {
                throwList.add(thrown)
            }
        }
        if (throwList.isNotEmpty()) {
            return false
        }
        info.finalizeResult(sorted.size)
        configurationComplete.trigger(info)
        unprioritized.applyConfig(this,  receiver, Unit)
        return true
    }

    /**
     * Applies configuration across all DSL groups for the given [receiver], supplying
     * an external [parameter] to each group's configuration logic.
     *
     * The groups are executed in ascending priority order. Any exception produced by
     * a group is collected; no exception is thrown during the process.
     *
     * @param receiver The target object to configure.
     * @param parameter The additional parameter to pass into each DSL group.
     *
     * @return `true` if all groups executed without exceptions, or `false` if one or
     *         more DSL groups returned an error.
     */
    fun <P> applyConfig(receiver: T, parameter: P): Boolean {
        val throwList = mutableListOf<Throwable>()
        val sorted = dslGroups.entries.sortedBy { it.key }.map { it.value }
        val info = ConfiguratorInfo(this, sorted.size, sorted.sumOf { it.configurators.size })
        configurationStart.trigger(info)

        sorted.forEach {
            val thrown = runGroupConfig(receiver, parameter, it.priority, singleLaunch = false)
            if (thrown != null) {
                throwList.add(thrown)
            }
        }
        info.finalizeResult(sorted.size)
        configurationComplete.trigger(info)
        return throwList.isNotEmpty()
    }

    /**
     * Applies configuration to the given [receiver] with the supplied [parameter],
     * but only for the DSL group identified by [priority].
     *
     * If the group executes without error, the method returns `true`.
     * If the underlying configuration logic produced an exception, this method
     * returns `false` instead of throwing.
     *
     * @param receiver The target object to configure.
     * @param parameter An optional additional parameter supplied to the DSL group.
     * @param priority The DSL group identifier.
     *
     * @return `true` if the priority-group configuration completed successfully,
     *         or `false` if it produced an exception.
     */
    fun <P> applyConfig(receiver: T, parameter: P, priority: HasNameValue): Boolean {
        runGroupConfig(receiver, parameter, priority, singleLaunch = true) ?: return true
        return false
    }

    /**
     * Applies configuration to the given [receiver] with the supplied [parameter],
     * for the DSL group identified by [priority]. Exceptions inside the DSL group
     * are not swallowed — they are rethrown immediately.
     *
     * This is the strict variant of the priority-based configurator.
     *
     * @param receiver The target of the configuration.
     * @param parameter The additional argument provided to DSL logic.
     * @param priority The DSL group to execute.
     * @param throwing Marker parameter indicating that exceptions should be propagated.
     *
     * @throws Throwable If the selected DSL group fails.
     */
    fun <P> applyConfig(receiver: T, parameter: P, priority: HasNameValue, throwing: Throwing) {
        runGroupConfig(receiver, parameter, priority, singleLaunch = true)?.let {
            throw it
        }
    }

    override fun notify(logMessage: LogMessage): LogMessage {
        logProcessor.logData(logMessage)
        return logMessage
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