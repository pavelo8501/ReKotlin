package po.misc.context

import po.misc.data.logging.LogEmitter
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.SpecialChars
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/**
 * Base interface for any context-aware component.
 *
 * Provides access to the unique [CTXIdentity] that describes the context type,
 * including optional parent context information for hierarchical resolution.
 */
interface CTX : LogEmitter {

    val identity: CTXIdentity<out CTX>

    private fun getIdentityWithFallback():CTXIdentity<out CTX>{
        val errorMsg = "identity requested while not constructed. Providing fake one." + SpecialChars.NewLine.char +
        "Most common reason is initialization of identity dependant properties in abstract class"
       return identity?:run {
           notify(errorMsg, SeverityLevel.WARNING)
           CTXIdentity(CTX::class,  CTX::class.starProjectedType, 0, )
       }
    }

    val contextName: String get() = getIdentityWithFallback().className
    val completeName: String get() = getIdentityWithFallback().completeName
    val identifiedByName: String get() = getIdentityWithFallback().identifiedByName
    val detailedDump: String get() = getIdentityWithFallback().detailedDump
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
