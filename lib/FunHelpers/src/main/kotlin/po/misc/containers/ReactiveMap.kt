package po.misc.containers

import po.misc.data.language.MessageBundle
import po.misc.exceptions.ManagedException
import po.misc.functions.common.ExceptionFallback
import po.misc.functions.hooks.ChangeHook
import po.misc.functions.hooks.ErrorHook
import po.misc.functions.hooks.models.ErrorData
import po.misc.functions.models.Updated
import po.misc.types.safeCast
import kotlin.reflect.KClass


class ReactiveMap<K: Any, V: Any>(

): AbstractMutableMap<K, V>(){

    @PublishedApi
    internal val mapBacking: MutableMap<K, V> = mutableMapOf()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = mapBacking.entries

    val itemsSize: Int get() = entries.size
    val onErrorHook: ErrorHook<ReactiveMap<K, V>> = ErrorHook()
    val onNewEntryHook: ChangeHook<V> = ChangeHook()

    private var exceptionFallback: ExceptionFallback? = null
    var onExceptionSnapshot:(()-> String)? = null

    @PublishedApi
    internal fun proceedWithFallback(key:K): Nothing{
        throw  exceptionFallback?.exceptionProvider?.invoke()?: run {
            throw ManagedException(this, MessageBundle.get("NotFoundError", key))
        }
    }

    @PublishedApi
    internal fun proceedWithFallback(kClass: KClass<*>): Nothing{
        val exception =  exceptionFallback?.exceptionProvider?.invoke()?: run {
             ManagedException(this, MessageBundle.get("CastError", kClass))
        }
        onErrorHook.trigger(ErrorData(this, exception, onExceptionSnapshot?.invoke()?:""))
        throw exception
    }

    override fun put(key: K, value: V): V? {
        val previous = mapBacking.put(key, value)
        onNewEntryHook.trigger(Updated(previous, value))

        return mapBacking.put(key, value)
    }

    fun getUnsafe(key:K):V{
        return mapBacking[key] ?: proceedWithFallback(key)
    }

    inline fun <reified V: Any> getUnsafeCasting(key:K):V{

        val result = mapBacking[key] ?: proceedWithFallback(key)
        val kClass = V::class
        return result.safeCast(kClass)?:proceedWithFallback(kClass)
    }

    fun injectFallback(fallback:()-> Throwable){
        exceptionFallback = ExceptionFallback(fallback)
    }
}