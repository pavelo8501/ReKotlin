package po.misc.context



/**
 * Base interface for any context-aware component.
 *
 * Provides access to the unique [CTXIdentity] that describes the context type,
 * including optional parent context information for hierarchical resolution.
 */
interface CTX{

    val identity: CTXIdentity<out  CTX>
    val completeName: String get() = identity.completeName
    val contextName: String get() = identity.name
    val qualifiedName: String get() = identity.qualifiedName
    val parentIdentity:CTXIdentity<*>? get() = identity.parentIdentity
}

/**
 * Specialized [CTX] that exposes a strongly-typed identity.
 *
 * This interface enforces F-bounded polymorphism so that the identity type matches the implementing type,
 * enabling safe and type-preserving identity access in generic APIs.
 *
 * @param T The concrete subtype that implements this interface.
 */
interface Identifiable<T: Identifiable<T>>: CTX {
    override val identity: CTXIdentity<T>
}


//interface Identifiable {
//    val sourceName: String
//    val context: CTX
//   // val contextName: String get() = context.contextName
//
//    /**
//     * A derived hierarchical name composed of the [context]'s name and this [sourceName].
//     */
//
//    val completeName: String get() {
//      return  context?.let {
//            "${it.contextName}<$sourceName>"
//        }?:run {
//            sourceName
//        }
//    }
//}
//
//@Deprecated("To be depreciated", ReplaceWith("CTX"),  DeprecationLevel.WARNING)
//interface CtxId:Identifiable {
//    override val sourceName: String
//}









