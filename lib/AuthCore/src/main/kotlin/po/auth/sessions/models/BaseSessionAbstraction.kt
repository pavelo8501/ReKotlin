package po.auth.sessions.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.SessionLifecycleCallback
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext



/**
 * Base abstraction for  coroutine-aware session to be used as a base for session creation.
 */
abstract class BaseSessionAbstraction<PRINCIPAL>(

) : EmmitableSession,  CoroutineContext,  CoroutineScope
        where PRINCIPAL : CoroutineContext.Element  {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO + this

    val createdAt: Long = System.currentTimeMillis()

    abstract val internalStore : ConcurrentHashMap<String, String>

    val sessionStore: ConcurrentHashMap<SessionKey<*>, Any?> =  ConcurrentHashMap<SessionKey<*>, Any?>()
    val roundTripStore: ConcurrentHashMap<RoundTripKey<*>, Any?> = ConcurrentHashMap<RoundTripKey<*>, Any?>()
    val externalStore :ConcurrentHashMap<ExternalKey<*>, Any?>  = ConcurrentHashMap<ExternalKey<*>, Any?>()

    var lifecycleCallback: SessionLifecycleCallback? = null

     fun setAttribute(key: String, value: String) {
        internalStore[key] = value
    }
     fun getAttribute(key: String): String? = internalStore[key]

    inline  fun <reified T: Any > setSessionAttr(name: String, value: T) {
         sessionStore[SessionKey<T>(name, T::class)] = value
    }
    inline fun <reified T: Any?> getSessionAttr(name: String): T? {
        sessionStore.keys.firstOrNull{ it.name ==  name}?.let {
            return when(it.clazz.qualifiedName){
                T::class.qualifiedName ->{ sessionStore[it] as T? }
                else -> { null }
            }
        }?: return null
    }

    inline  fun <reified T: Any > setRoundTripAttr(name: String, value: T) {
        roundTripStore[RoundTripKey<T>(name, T::class)] = value
    }
    inline fun <reified T: Any?> getRoundTripAttr(name: String): T? {
        roundTripStore.keys.firstOrNull{ it.name ==  name}?.let {
            return when(it.clazz.qualifiedName){
                T::class.qualifiedName ->{ roundTripStore[it] as T? }
                else -> { null }
            }
        }?: return  null
    }

    inline  fun <reified T: Any > setExternalRef(name: String, value: T) {
        externalStore[ExternalKey<T>(name, T::class)] = value
    }
    inline fun <reified T: Any?> getExternalRef(name: String): T? {
        externalStore.keys.firstOrNull{ it.name ==  name}?.let {
            return when(it.clazz.qualifiedName){
                T::class.qualifiedName ->{ externalStore[it] as T? }
                else -> { null }
            }
        }?: return  null
    }

    companion object {
        fun generateSessionId(): String = UUID.randomUUID().toString()
    }

}