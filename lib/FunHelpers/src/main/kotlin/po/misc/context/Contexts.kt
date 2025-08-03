package po.misc.context

import po.misc.data.logging.LogEmitter
import po.misc.data.printable.PrintableBase
import po.misc.data.printable.companion.PrintableTemplateBase
import po.misc.data.processors.Logger

/**
 * Base interface for any context-aware component.
 *
 * Provides access to the unique [CTXIdentity] that describes the context type,
 * including optional parent context information for hierarchical resolution.
 */
interface CTX : LogEmitter {
    val identity: CTXIdentity<out CTX>

    val contextName: String get() = identity.className
    val completeName: String get() = identity.completeName
    val identifiedByName: String get() = identity.identifiedByName
    val detailedDump: String get() = identity.detailedDump
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
