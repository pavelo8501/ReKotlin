package po.auth.sessions.models

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import po.auth.authentication.authenticator.UserAuthenticator
import po.auth.authentication.authenticator.models.AuthenticationPrincipal
import po.auth.sessions.enumerators.SessionType
import po.auth.sessions.interfaces.EmmitableSession
import po.auth.sessions.interfaces.SessionIdentified
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.printable.PrintableBase
import po.misc.interfaces.Processable
import po.misc.time.ExecutionTimeStamp
import po.misc.types.castOrManaged
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class AuthorizedSession internal constructor(
    override val remoteAddress: String,
    val authenticator: UserAuthenticator,
): EmmitableSession, SessionIdentified, Processable {

    override val identity: CTXIdentity<AuthorizedSession> = asIdentity()
    val timeStamp: ExecutionTimeStamp = ExecutionTimeStamp()

    var principal : AuthenticationPrincipal? = null
    override var sessionType: SessionType = SessionType.ANONYMOUS
        private set

    @PublishedApi
    internal val logRecordsBacking: MutableList<PrintableBase<*>> = mutableListOf()
    val logRecords: List<PrintableBase<*>> = logRecordsBacking

    override val sessionID: String = UUID.randomUUID().toString()

    override val sessionContext: CoroutineContext get() = scope.coroutineContext
    override val scope: CoroutineScope by lazy { CoroutineScope(CoroutineName(identifiedByName) + this) }

    val coroutineContext: CoroutineContext get() = scope.coroutineContext

    init {
        identity.setNamePattern {
             timeStamp.provideNameAndId(sessionType.sessionName, sessionID)
            "${sessionType.sessionName}[$sessionID]"
        }
    }
    override fun onProcessStart(session: EmmitableSession) {

    }
    override fun onProcessEnd(session: EmmitableSession) {

    }

    override val key: CoroutineContext.Key<AuthorizedSession> get() = AuthorizedSessionKey

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

    inline  fun <reified T: Any> setExternalRef(identifiedBy: Any, value: T) {
        externalStore[ExternalKey(identifiedBy.toString(), T::class)] = value
    }

    inline fun <reified T: Any> getExternalRef(identifiedBy: Any): T? {
        externalStore.keys.firstOrNull{ it.name ==  identifiedBy.toString()}?.let { key ->
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

    override fun provideData(record: PrintableBase<*>) {
        logRecordsBacking.add(record)
    }

   inline fun <reified T: PrintableBase<T>> extractLog(
       noinline filtering:((T)-> Boolean)? = null
   ): List<T> {
        val resultingList = mutableListOf<T>()
        return if (filtering != null) {
            val filtered = logRecordsBacking.filterIsInstance<T>()
            filtered.forEach { record ->
                if (filtering.invoke(record)) {
                    resultingList.add(record)
                }
            }
            resultingList
        } else {
            logRecordsBacking.filterIsInstance<T>()
        }
    }

    fun extractLogRecords(filtering:((PrintableBase<*>)-> Boolean)? = null): List<PrintableBase<*>> {
        val resultingList = mutableListOf<PrintableBase<*>>()
        return if (filtering != null) {
            logRecordsBacking.forEach { record ->
                if (filtering.invoke(record)) {
                    resultingList.add(record)
                }
            }
            resultingList
        } else {
            logRecordsBacking
        }
    }

    companion object AuthorizedSessionKey : CoroutineContext.Key<AuthorizedSession>

}



