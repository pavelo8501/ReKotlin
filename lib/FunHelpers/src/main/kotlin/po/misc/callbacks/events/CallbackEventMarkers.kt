package po.misc.callbacks.events

import po.misc.data.helpers.output
import po.misc.data.styles.Colour
import po.misc.types.TypeData



sealed interface TypedParameters

/**
 * Marker interface for classes that can host parameterized events.
 *
 * Typically implemented by components or contexts that expose events
 * (e.g. `Form`, `ChannelContext`, `Session`).
 *
 * Acts as the "owner" or "source" of events, allowing listeners
 * to bind to a particular host instance.
 */
interface EventHost : TypedParameters



/**
 * Registers a listener that will be invoked whenever [event] is triggered.
 *
 * @param L the type of the listener (usually the class receiving the callback).
 * @param T the type of event payload.
 * @param listener the instance that owns the callback (used for identification/unsubscription).
 * @param event the event to subscribe to.
 * @param onTriggered the callback to invoke when [event] is fired.
 *
 * Example:
 * ```
 * form.listenTriggered(this, form.onSubmit) { data ->
 *     println("Form submitted with: $data")
 * }
 * ```
 */
inline fun <reified L: Any, T: Any> EventHost.listenTriggered(
    listener: L,
    event:  CallbackEventBase<*, T, *>,
    noinline onTriggered: (T)-> Unit
){
    event.listeners.onEventTriggered(listener, onTriggered)
}

/**
 * Creates a [TypeData] descriptor for the current type [T].
 *
 * Useful for providing reflective type information when building
 * or registering events and parameters.
 *
 * Example:
 * ```
 * val typeData = myEvent.createTypeData()
 * ```
 */
inline fun <reified T: TypedParameters> T.createTypeData(): TypeData<T>{
   return TypeData.create<T>()
}

inline fun <reified T: Any> EventHost.typeDataOf(): TypeData<T>{
    return TypeData.create<T>()
}

