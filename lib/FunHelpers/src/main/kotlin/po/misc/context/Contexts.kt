package po.misc.context

import po.misc.data.output.output
import po.misc.data.logging.LogEmitter
import po.misc.data.styles.SpecialChars
import po.misc.exceptions.ContextTracer
import po.misc.context.tracable.TraceableContext
import po.misc.exceptions.models.CTXResolutionFlag
import po.misc.types.token.TypeToken

/**
 * Base interface for all context-aware objects.
 *
 * ## Purpose
 * - Provides a unique [identity] to identify this context instance.
 * - Supplies a [emitter] to log events or trace information.
 * - Offers convenience properties for accessing context information:
 *   - [contextName]
 *   - [completeName]
 *   - [identifiedByName]
 *   - [detailedDump]
 *
 * ## Identity
 * - The [identity] property is a [CTXIdentity] that is usually initialized via the
 *   helper function [asIdentity()] from the `CTX` framework.
 * - If identity is accessed before full construction, a **fallback identity** is
 *   automatically provided with a warning. See [getIdentityWithFallback].
 *
 * ## Example usage
 * ```
 * class TestContextAware: ContextAware {
 *     override val identity: CTXIdentity<TestContextAware> = asIdentity()
 *     override val emitter: ContextAwareLogEmitter = logEmitter()
 *
 *     init {
 *         identity.setNamePattern { "TestContextAware(Something)" }
 *     }
 * }
 * ```
 *
 * ## Notes
 * - [CTX] is the base for all contexts that want to participate in logging/tracing.
 * - Helper functions like [asIdentity()] and [logEmitter()] exist to simplify wiring.
 * - All identity-dependent properties automatically use [getIdentityWithFallback] to
 *   avoid null pointer issues during early initialization.
 */
interface CTX : LogEmitter, TraceableContext {

    val identity: CTXIdentity<out CTX>

    private fun getIdentityWithFallback():CTXIdentity<out CTX>{
        val errorMsg = "identity requested while not constructed. Providing fake one." + SpecialChars.NEW_LINE +
        "Most common reason is initialization of identity dependant properties in abstract class"
       @Suppress("USELESS_ELVIS")
       return identity?:run {
          val trace = ContextTracer(this,  CTXResolutionFlag.NoResolution, errorMsg)
           trace.output()
           CTXIdentity(TypeToken.create<CTX>(),  0)
       }
    }

    val contextName: String get() = getIdentityWithFallback().className
    val completeName: String get() = getIdentityWithFallback().completeName
    val identifiedByName: String get() = getIdentityWithFallback().identifiedByName
    val detailedDump: String get() = getIdentityWithFallback().detailedDump.toString()
}

/**
 * Specialized [CTX] that exposes a strongly-typed identity.
 *
 * This interface enforces F-bounded polymorphism so that the identity type matches the implementing type,
 * enabling safe and type-preserving identity access in generic APIs.
 *
 * @param T The concrete subtype that implements this interface.
 */
interface Identifiable<T : Identifiable<T>> : CTX {
    override val identity: CTXIdentity<T>
}
