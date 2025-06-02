package po.auth.sessions.models

import io.ktor.server.application.ApplicationCall
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.authentication.exceptions.AuthException
import po.auth.authentication.exceptions.ErrorCodes
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.SessionIdentified
import po.lognotify.classes.notification.models.Notification
import po.lognotify.classes.process.LoggProcess
import po.lognotify.classes.process.ProcessableContext
import po.misc.types.castOrThrow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class AuthorizedSession internal constructor(
    override val remoteAddress: String,
    val authenticator: UserAuthenticator,
):  CoroutineContext.Element,  EmmitableSession, SessionIdentified, ProcessableContext<AuthorizedSession> {

    var principal : AuthenticationPrincipal? = null
    override var sessionType: SessionType = SessionType.ANONYMOUS
        private set

    val coroutineName : String get() {
        return if(sessionType == SessionType.ANONYMOUS){
            "AnonymousSession"
        }else{
            "AuthenticatedSession"
        }
    }

    override var getLoggerProcess: (() -> LoggProcess<*> )? = null

    override val sessionID: String = UUID.randomUUID().toString()
    override val sessionContext: CoroutineContext
        get() = scope.coroutineContext


    override val identifiedAs: String get() = sessionID


    override val name: String get() =  coroutineName

    private val scope: CoroutineScope = CoroutineScope(CoroutineName(coroutineName) + this)


    override fun onNotification(notification: Notification) {
        TODO("Not yet implemented")
    }

    override fun onProcessStart(session: LoggProcess<*>) {
        println("onProcessStart emitted with sessionId ${session.identifiedAs}")
    }

    override fun onProcessEnd(session: LoggProcess<*>) {
        println("onProcessEnd emitted with sessionId ${session.identifiedAs}")
    }

    override fun sessionScope(): CoroutineScope{
        println("Redispatched session $sessionID")
        return scope
    }

    override fun onProcessStart(session: EmmitableSession) {

    }
    override fun onProcessEnd(session: EmmitableSession) {

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

    internal inline fun <reified T: Any> getSessionAttr(name: String): T? {
        sessionStore.keys.firstOrNull{ it.name ==  name}?.let {key->
            val sessionParam = sessionStore[key].castOrThrow<T, AuthException>(
                "SessionStore item not found by key",
                ErrorCodes.SESSION_PARAM_FAILURE.value)

            return sessionParam
        }
        return null
    }

    inline  fun <reified T: Any> setRoundTripAttr(name: String, value: T) {
        roundTripStore[RoundTripKey(name, T::class)] = value
    }

    internal inline fun <reified T: Any> getRoundTripAttr(name: String): T? {
        roundTripStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = roundTripStore[key].castOrThrow<T, AuthException>(
                "SessionStore item not found by key",
                ErrorCodes.SESSION_PARAM_FAILURE.value)

            return sessionParam
        }
        return null
    }

    inline  fun <reified T: Any> setExternalRef(name: String, value: T) {
        externalStore[ExternalKey(name, T::class)] = value
    }

    internal inline fun <reified T: Any> getExternalRef(name: String): T? {
        externalStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = externalStore[key].castOrThrow<T, AuthException>(
                "SessionStore item not found by key",
                ErrorCodes.SESSION_PARAM_FAILURE.value)

            return sessionParam
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



