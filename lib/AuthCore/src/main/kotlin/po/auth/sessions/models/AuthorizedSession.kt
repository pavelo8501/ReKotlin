package po.auth.sessions.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.auth.AuthSessionManager
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


class AuthorizedSession(
    override val principal : AuthorizedPrincipal,
    val sessionType: SessionType,
    val internalStore : ConcurrentHashMap<String, String>,
):  AbstractCoroutineContextElement(AuthSessionManager.AuthorizedSessionKey),  EmmitableSession {
    override val sessionId: String = generateSessionId()
    override val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + this)


    override fun onProcessStart(session: EmmitableSession) {
        TODO("Not yet implemented")
    }
    override fun onProcessEnd(session: EmmitableSession) {
        TODO("Not yet implemented")
    }

    val sessionStore: ConcurrentHashMap<SessionKey<*>, Any?> =  ConcurrentHashMap<SessionKey<*>, Any?>()
    val roundTripStore: ConcurrentHashMap<RoundTripKey<*>, Any?> = ConcurrentHashMap<RoundTripKey<*>, Any?>()
    val externalStore :ConcurrentHashMap<ExternalKey<*>, Any?>  = ConcurrentHashMap<ExternalKey<*>, Any?>()


    fun getAttributeKeys():List<SessionKey<*>>{
        return sessionStore.keys.toList()
    }
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



