package po.auth.sessions.models

import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.authentication.extensions.castOrThrow
import po.auth.authentication.extensions.getOrThrow
import po.auth.authentication.interfaces.AuthenticationPrincipal
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.SessionIdentified
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext


class AuthorizedSession internal constructor(
    override val remoteAddress: String,
    val authenticator: UserAuthenticator
):  AbstractCoroutineContextElement(AuthorizedSession),  EmmitableSession, SessionIdentified {


    var principal : AuthenticationPrincipal? = null
    override var sessionType: SessionType = SessionType.ANONYMOUS
        private set

    override val sessionId: String = UUID.randomUUID().toString()
    override val scope: CoroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("AuthorizedSession") + this)

    override fun onProcessStart(session: EmmitableSession) {
        println("onProcessStart emitted with sessionId ${session.sessionId}")
    }
    override fun onProcessEnd(session: EmmitableSession) {
        println("onProcessEnd emitted with sessionId ${session.sessionId}")
    }

    override val key: CoroutineContext.Key<AuthorizedSession>
        get() {
            return AuthorizedSessionKey
        }

    val sessionStore: ConcurrentHashMap<SessionKey<*>, Any?> =  ConcurrentHashMap<SessionKey<*>, Any?>()
    val roundTripStore: ConcurrentHashMap<RoundTripKey<*>, Any?> = ConcurrentHashMap<RoundTripKey<*>, Any?>()
    val externalStore :ConcurrentHashMap<ExternalKey<*>, Any?>  = ConcurrentHashMap<ExternalKey<*>, Any?>()

    fun getAttributeKeys():List<SessionKey<*>>{
        return sessionStore.keys.toList()
    }
    inline  fun <reified T: Any> setSessionAttr(name: String, value: T) {
        sessionStore[SessionKey(name, T::class)] = value
    }
    inline fun <reified T: Any> getSessionAttr(name: String): T? {

        sessionStore.keys.firstOrNull{ it.name ==  name}?.let {key->
            val sessionParam = sessionStore[key].getOrThrow("SessionStore item not found by key", ErrorCodes.ABNORMAL_STATE)
            return sessionParam.castOrThrow<T>("Cast Failed", ErrorCodes.ABNORMAL_STATE)
        }
        return null
    }

    inline  fun <reified T: Any> setRoundTripAttr(name: String, value: T) {
        roundTripStore[RoundTripKey(name, T::class)] = value
    }

    inline fun <reified T: Any> getRoundTripAttr(name: String): T? {

        roundTripStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = roundTripStore[key].getOrThrow("SessionStore item not found by key", ErrorCodes.ABNORMAL_STATE)
            return sessionParam.castOrThrow<T>("Cast Failed", ErrorCodes.ABNORMAL_STATE)
        }
        return null
    }

    inline  fun <reified T: Any> setExternalRef(name: String, value: T) {
        externalStore[ExternalKey(name, T::class)] = value
    }
    inline fun <reified T: Any> getExternalRef(name: String): T? {
        externalStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = externalStore[key].getOrThrow("SessionStore item not found by key", ErrorCodes.ABNORMAL_STATE)
            return sessionParam.castOrThrow<T>("Cast Failed", ErrorCodes.ABNORMAL_STATE)
        }
        return null
    }

   inline fun <reified T: CoroutineScope> storeConsumed(value:T){
       setExternalRef(T::class.simpleName.toString(), value)
   }

    fun providePrincipal(authenticationPrincipal: AuthenticationPrincipal):AuthorizedSession {
        principal = authenticationPrincipal
        sessionType  = SessionType.USER_AUTHENTICATED
        return this
    }

    suspend fun <T,R> useContext(context: CoroutineContext, receiver: T,  block: suspend T.() -> R){
        withContext(context) {
            async(start = CoroutineStart.UNDISPATCHED) {
                block.invoke(receiver)
            }
        }
    }

    suspend fun reLaunch(call: ApplicationCall){
        withContext(this){
            launch {
                call.coroutineContext
            }
        }
    }

    companion object AuthorizedSessionKey : CoroutineContext.Key<AuthorizedSession>

}



