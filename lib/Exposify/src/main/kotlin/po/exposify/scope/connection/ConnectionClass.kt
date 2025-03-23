package po.exposify.scope.connection

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.interfaces.ManagedSession
import po.exposify.classes.interfaces.DataModel
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceClass
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify

class ConnectionClass(
    val connectionInfo: ConnectionInfo,
    val connection: Database,
    val sessionManager: ManagedSession
) : CanNotify {

    val name = "ConnectionClas|{$connection.name}"

    var services = mutableMapOf<String, ServiceClass<*, *>>()

    override var eventHandler = RootEventHandler(name)

    init {
        connectionInfo.connection
    }

    private val dispatchManager = UserDispatchManager()
    private val coroutineEmitter = CoroutineEmitter("${connectionInfo.dbName}|CoroutineEmitter", eventHandler)

    val isConnectionOpen: Boolean
        get() {
            return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false
        }

    suspend fun <DATA : DataModel, ENTITY : LongEntity> launchSequence(
        pack: SequencePack<DATA, ENTITY>,
        parentEventHandler: RootEventHandler //Temporary solution unless sessions will get introduced
    ): Deferred<List<DATA>> {

        sessionManager.getCurrentSession()?.let { emmitableSession ->
            CoroutineScope(Dispatchers.IO).async {
                dispatchManager.queManager.enqueue(emmitableSession.principal.userId, this) {
                    coroutineEmitter.dispatch(pack, this).await()
                }
            }

        } ?: run {
            sessionManager.getAnonymous()?.let {
                coroutineEmitter.dispatch(pack, it)
            }

        }

     return   CompletableDeferred<List<DATA>>(emptyList())

    }


    fun addService(service : ServiceClass<*,*>){
        services.putIfAbsent(service.name, service)
    }

    fun getService(name: String): ServiceClass<*,*>?{
        this.services.keys.firstOrNull{it == name}?.let {
            return services[it]
        }
        return null
    }


}