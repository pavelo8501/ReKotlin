package po.misc.containers

import po.misc.exceptions.ManagedCallSitePayload
import po.misc.exceptions.ManagedException
import po.misc.exceptions.ManagedPayload
import po.misc.functions.common.ExceptionFallback
import po.misc.types.Typed
import po.misc.types.castOrManaged
import po.misc.types.getOrManaged
import po.misc.types.safeCast
import kotlin.reflect.KClass


class ReactiveMap<K: Any, V: Any>(

): AbstractMutableMap<K, V>(){

    @PublishedApi
    internal val mapBacking: MutableMap<K, V> = mutableMapOf()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = mapBacking.entries

    val itemsSize: Int get() = entries.size

    private var exceptionFallback: ExceptionFallback? = null

    private val notFoundError: (key:K)->String = {"Element with key $it not found"}
    private val castError: (kClass: KClass<*>)->String = {"Record exist but cast to $it failed"}

    @PublishedApi
    internal fun proceedWithFallback(key:K): Nothing{
        val payload = ManagedPayload(notFoundError(key), "proceedWithFallback",  this)
        throw  exceptionFallback?.exceptionProvider?.invoke(payload)?: run {
            throw ManagedException(notFoundError(key))
        }
    }

    @PublishedApi
    internal fun proceedWithFallback(kClass: KClass<*>): Nothing{

        val payload = ManagedPayload(castError(kClass), "proceedWithFallback",  this)
        throw  exceptionFallback?.exceptionProvider?.invoke(payload)?: run {
            throw ManagedException(castError(kClass))
        }
    }

    override fun put(key: K, value: V): V? {
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

    fun injectFallback(fallback:(ManagedCallSitePayload)-> Throwable){
        exceptionFallback = ExceptionFallback(fallback)
    }
}