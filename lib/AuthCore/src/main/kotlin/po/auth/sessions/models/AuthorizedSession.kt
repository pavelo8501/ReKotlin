package po.auth.sessions.models

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import po.auth.AuthSessionManager
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.castOrThrow
import po.auth.authentication.extensions.getOrThrow
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.lognotify.extensions.castOrException
import po.lognotify.extensions.getOrException
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
        sessionStore[SessionKey(name, T::class)] = value
    }
    inline fun <reified T: Any> getSessionAttr(name: String): T? {

        sessionStore.keys.firstOrNull{ it.name ==  name}?.let {key->
            val sessionParam = sessionStore[key].getOrThrow("SessionStore item not found by key", ErrorCodes.ABNORMAL_STATE)
            return sessionParam.castOrThrow<T>("Cast Failed", ErrorCodes.ABNORMAL_STATE)
        }
        return null
    }

    inline  fun <reified T: Any > setRoundTripAttr(name: String, value: T) {
        roundTripStore[RoundTripKey(name, T::class)] = value
    }

    inline fun <reified T: Any> getRoundTripAttr(name: String): T? {

        roundTripStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = roundTripStore[key].getOrThrow("SessionStore item not found by key", ErrorCodes.ABNORMAL_STATE)
            return sessionParam.castOrThrow<T>("Cast Failed", ErrorCodes.ABNORMAL_STATE)
        }
        return null
    }

    inline  fun <reified T: Any > setExternalRef(name: String, value: T) {
        externalStore[ExternalKey(name, T::class)] = value
    }
    inline fun <reified T: Any> getExternalRef(name: String): T? {
        externalStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = externalStore[key].getOrThrow("SessionStore item not found by key", ErrorCodes.ABNORMAL_STATE)
            return sessionParam.castOrThrow<T>("Cast Failed", ErrorCodes.ABNORMAL_STATE)
        }
        return null
    }


    companion object {
        fun generateSessionId(): String = UUID.randomUUID().toString()
    }
}



