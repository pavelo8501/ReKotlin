package po.misc.containers


/**
 * A container interface that holds a [receiver] of type [T].
 * Typically used to delegate method scope and make the receiver's context available
 * in lambda expressions via [withReceiver] or [withReceiverAndResult].
 * @param T The type of the contained receiver object.
 */
interface ReceiverContainer<T: Any>{
    /** The wrapped receiver whose members will be exposed in scope-aware blocks. */
    val receiver: T
}
