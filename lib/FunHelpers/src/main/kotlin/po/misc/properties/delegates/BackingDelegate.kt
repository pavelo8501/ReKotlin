package po.misc.properties.delegates

import po.misc.exceptions.ThrowableCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.functions.hooks.DataHooks
import po.misc.functions.hooks.DataNotifier
import po.misc.context.CTX
import po.misc.context.asIdentity
import po.misc.functions.models.Updated
import po.misc.types.getOrManaged
import po.misc.types.token.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * A property delegate that defers the initialization of a value and enforces access validation.
 *
 * This delegate is intended for scenarios where a value must be externally provided
 * before it is accessed. If the value is accessed before being set, a failure is reported
 * using the supplied [ThrowableCallSitePayload].
 *
 * Useful in dependency wiring, reactive graphs, and systems with controlled lifecycle stages.
 *
 * @param  T the type of the delegated value.
 * @property typeToken metadata about the expected type, used for more informative error messages.
 * @property receiver  the payload used to report an error when the value is accessed before being set.
 */
class BackingDelegate<T : Any>(
    private val typeToken: TypeToken<T>,
    private val receiver :T ? = null,
    private val configure: (DataHooks<BackingDelegate<T>, T>.() -> Unit)? = null
) : ReadWriteProperty<Any?, T>, CTX{

    private var value: T? = null

    override val identity  = asIdentity()

    private val notifier : DataNotifier<BackingDelegate<T>, T> = DataNotifier(this)

    override val contextName: String get() = identity.completeName

    val isValueSet: Boolean
        get() = value != null

    init {
        configure?.invoke(notifier)
        notifier.triggerBeforeInitialized()
        receiver?.let {
            valueProvided(it)
        }
    }

    private fun valueProvided(data:T){
        val previousValue = value
        value = data
        if(previousValue == null){
            notifier.triggerInitialized()
        }
        notifier.triggerChanged(Updated(previousValue, data))
    }

    /**
     * Returns the delegated value if available.
     * @throws ManagedException if the value has not been set.
     */
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value.getOrManaged(this, typeToken.kClass)
    }

    /**
     * Assigns the delegated value.
     * @param value the value to be stored and later accessed.
     */
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        valueProvided(value)
    }

    companion object {
        /**
         * Creates a [BackingDelegate] for the given type using reified generics.
         * This automatically infers the [TypeData] from the reified type parameter.
         * @param T the type of the backing value.
         * @return a new [BackingDelegate] instance.
         */
        inline fun <reified T : Any> create(receiver:T ? = null): BackingDelegate<T> =
            BackingDelegate(TypeToken.create<T>(), receiver)

        /**
         * Creates a [BackingDelegate] for the given type.
         * @param T the type of the backing value.
         * @param typeToken the [TypeToken] of the backing value.
         * @return a new [BackingDelegate] instance.
         */
        fun <T : Any> create(typeData: TypeToken<T>, receiver: T? = null): BackingDelegate<T> =
            BackingDelegate(typeData, receiver)


        /**
         * Creates a [BackingDelegate] for the given type.
         * @param receiver of [T] optional initial value.
         * @param configure responsive hooks of type [DataHooks].
         * @return a new [BackingDelegate] instance.
         */
        inline  fun <reified T : Any> withHooks(
            receiver: T? = null,
            noinline configure: DataHooks<BackingDelegate<T>, T>.() -> Unit
        ): BackingDelegate<T> = BackingDelegate(TypeToken.create<T>(), receiver, configure)

    }
}