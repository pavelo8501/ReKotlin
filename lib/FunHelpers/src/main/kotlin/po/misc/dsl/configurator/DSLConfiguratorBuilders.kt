package po.misc.dsl.configurator

import po.misc.context.tracable.TraceableContext
import po.misc.data.HasNameValue
import kotlin.enums.enumEntries


/**
 * Creates a DSLConfigurator using an already prepared collection of DSL groups.
 * @param dslGroups The groups to initialize the configurator with.
 */
fun <T: TraceableContext> dslConfigFromGroups(
    dslGroups: Collection<DSLConfigurable<T, *>>,
): DSLConfigurator<T>{
    val configurator = DSLConfigurator(dslGroups)
    return configurator
}

/**
 * Creates a DSLConfigurator with priority groups derived from an enum type.
 *
 * This inline function uses reified generics to automatically create configuration groups
 * for each entry in the specified enum class. The enum must implement [HasNameValue].
 *
 * @param T The traceable context type that configurations will be applied to
 * @param E The enum type that defines the priority groups and implements [HasNameValue]
 * @return A DSLConfigurator instance with groups for each enum entry
 *
 *
 * @see dslConfigurator with explicit Class parameter for non-inline usage
 * @see HasNameValue interface for priority contract
 *
 * @example
 * ```
 * // Creates groups for ConfigPriority.Top, ConfigPriority.Default, etc.
 * val configurator = dslConfigurator<Configurable, ConfigPriority>()
 * ```
 */
inline fun <T: TraceableContext, reified  E> dslConfigFromEnum(): DSLConfigurator<T> where E : HasNameValue, E : Enum<E>{
   val groups  = enumEntries<E>().map {
        DSLGroup<T>(it)
    }
    val configurator = dslConfigFromGroups(groups)
    return configurator
}

/**
 * Builds a DSLConfigurator using a standalone configuration block.
 *
 * This function creates a configurator without any implicit context.
 * Use this when you need to build configuration independently.
 *
 * @param T The traceable context type that configurations will be applied to
 * @param block Configuration DSL block to set up the configurator
 * @return A configured DSLConfigurator instance
 *
 * Usage:
 * ```
 * val config = dslConfig<Ctx> {
 *     buildGroup(...)
 * }
 * ```
 */
fun <T: TraceableContext> dslConfig(
    block : DSLConfigurator<T>.()-> Unit
): DSLConfigurator<T>{
    val configurator = DSLConfigurator<T>()
    configurator.block()
    return configurator
}

/**
 * Builds a DSLConfigurator using the current context as an **implicit receiver**.
 *
 * ⚠ **Important**: This version has an implicit `this` receiver of type `T`
 * in the configuration block, allowing direct access to context properties and methods.
 *
 * Use this when you want to build configurations within the context of a traceable object.
 *
 * @param T The traceable context type that serves as both receiver and configuration target
 * @param block Configuration DSL block that has implicit access to the context receiver
 * @return A configured DSLConfigurator instance
 *
 * Usage:
 * ```
 * val config = myContext.dslConfigForContext {
 *     buildGroup(...)
 * }
 * ```
 */
fun <T: TraceableContext> T.dslConfigForContext(
    block : DSLConfigurator<T>.()-> Unit
): DSLConfigurator<T>{
    val configurator = DSLConfigurator<T>()
    configurator.block()
    return configurator
}

