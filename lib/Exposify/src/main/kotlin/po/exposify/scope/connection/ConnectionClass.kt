package po.exposify.scope.connection

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.interfaces.ManagedSession
import po.exposify.classes.interfaces.DataModel
import po.exposify.controls.ConnectionInfo
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceClass

class ConnectionClass(
    val connectionInfo: ConnectionInfo,
    val connection: Database,
    val sessionManager: ManagedSession
) {

    val name: String = "ConnectionClas|{${connection.name}"

    init {
        connectionInfo.connection
    }

    private val dispatchManager = UserDispatchManager()
    private val coroutineEmitter = CoroutineEmitter("${connectionInfo.dbName}|CoroutineEmitter")

    val isConnectionOpen: Boolean
        get() { return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false }

    suspend fun <DTO:ModelDTO, DATA: DataModel> launchSequence(
        pack: SequencePack<DTO, DATA>,
    ): List<DATA> {
        val session = sessionManager.getCurrentSession()
        val result = dispatchManager.enqueue(session.sessionID) {
            coroutineEmitter.dispatch(pack, session)
        }
        return result
    }

    var services: MutableMap<String, ServiceClass<*, *, *>>
            = mutableMapOf()

    fun addService(serviceClass : ServiceClass<*, *, *>){
        services[serviceClass.personalName] = serviceClass
    }
    fun clearServices(){
        services.clear()
    }

    fun getService(name: String): ServiceClass<*, *, *>?{
        this.services.keys.firstOrNull{it == name}?.let { return services[it] }
        return null
    }
}