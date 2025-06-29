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
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.SessionIdentified
import po.lognotify.process.LoggerProcess
import po.misc.coroutines.CoroutineHolder
import po.misc.types.castOrManaged
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class AuthorizedSession internal constructor(
    override val remoteAddress: String,
    val authenticator: UserAuthenticator,
):  CoroutineContext.Element,  EmmitableSession, SessionIdentified, CoroutineHolder {

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

    //var getLoggerProcess: (() -> LoggProcess<*> )? = null

    override val sessionID: String = UUID.randomUUID().toString()

    override val sessionContext: CoroutineContext
        get() = scope.coroutineContext

    override val coroutineContext: CoroutineContext
        get() = scope.coroutineContext


    val identifiedAs: String get() = sessionID
    val name: String get() =  coroutineName
    private val scope: CoroutineScope = CoroutineScope(CoroutineName(coroutineName) + this)


    fun onProcessStart(session: LoggerProcess<*, *>) {
        println("onProcessStart emitted with sessionId ${session.identified}")
    }

    fun onProcessEnd(session: LoggerProcess<*, *>) {
        println("onProcessEnd emitted with sessionId ${session.identified}")
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
            val sessionParam = sessionStore[key].castOrManaged<T>("SessionStore item not found by key")
            return sessionParam
        }
        return null
    }

    inline  fun <reified T: Any> setRoundTripAttr(name: String, value: T) {
        roundTripStore[RoundTripKey(name, T::class)] = value
    }

    internal inline fun <reified T: Any> getRoundTripAttr(name: String): T? {
        roundTripStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = roundTripStore[key].castOrManaged<T>("SessionStore item not found by key")
            return sessionParam
        }
        return null
    }

    inline  fun <reified T: Any> setExternalRef(name: String, value: T) {
        externalStore[ExternalKey(name, T::class)] = value
    }

    internal inline fun <reified T: Any> getExternalRef(name: String): T? {
        externalStore.keys.firstOrNull{ it.name ==  name}?.let { key ->
            val sessionParam = externalStore[key].castOrManaged<T>("SessionStore item not found by key")
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



