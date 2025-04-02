package po.exposify.scope.connection

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.interfaces.ManagedSession
import po.exposify.classes.interfaces.DataModel
import po.exposify.controls.ConnectionInfo
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.connection.controls.CoroutineEmitter2
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.sequence.models.SequencePack2
import po.exposify.scope.service.ServiceClass

class ConnectionClass(
    val connectionInfo: ConnectionInfo,
    val connection: Database,
    val sessionManager: ManagedSession
) {

    val name: String = "ConnectionClas|{${connection.name}"

    var services: MutableMap<String, ServiceClass<*,*,*>> = mutableMapOf<String, ServiceClass<*,*,*>>()

//    override var eventHandler: RootEventHandler = RootEventHandler(name){
//        echo(it, "ConnectionClass: RootEventHandler")
//    }

    init {
        connectionInfo.connection
    }

    private val dispatchManager = UserDispatchManager()
    private val coroutineEmitter = CoroutineEmitter2("${connectionInfo.dbName}|CoroutineEmitter")


    val isConnectionOpen: Boolean
        get() {
            return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false
        }

    private fun <T> emptyList(): Deferred<List<T>>{
        return CompletableDeferred<List<T>>(emptyList<List<T>>())
    }

    suspend fun <DTO : ModelDTO> launchSequence(
        pack: SequencePack2<DTO>,
    ): Deferred<List<DataModel>> {
        val session = sessionManager.getCurrentSession()
            ?: sessionManager.getAnonymous()
        return session?.let {
            dispatchManager.enqueue(it.principal.userId) {
                coroutineEmitter.dispatch(pack, it.scope)
            }
        } ?: emptyList()
    }

    fun addService(service : ServiceClass<*,*,*>){
        services.putIfAbsent(service.name, service)
    }

    fun getService(name: String): ServiceClass<*,*,*>?{
        this.services.keys.firstOrNull{it == name}?.let { return services[it] }
        return null
    }
}