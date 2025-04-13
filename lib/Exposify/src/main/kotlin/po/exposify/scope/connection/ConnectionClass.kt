package po.exposify.scope.connection

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.interfaces.ManagedSession
import po.exposify.classes.interfaces.DataModel
import po.exposify.controls.ConnectionInfo
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.scope.connection.controls.CoroutineEmitter2
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceClass
import po.lognotify.extensions.getOrDefault

class ConnectionClass(
    val connectionInfo: ConnectionInfo,
    val connection: Database,
    val sessionManager: ManagedSession
) {

    val name: String = "ConnectionClas|{${connection.name}"

    var services: MutableMap<String, ServiceClass<ModelDTO, DataModel, ExposifyEntityBase>>
        = mutableMapOf<String, ServiceClass<ModelDTO, DataModel, ExposifyEntityBase>>()

    init {
        connectionInfo.connection
    }

    private val dispatchManager = UserDispatchManager()
    private val coroutineEmitter = CoroutineEmitter2("${connectionInfo.dbName}|CoroutineEmitter")


    val isConnectionOpen: Boolean
        get() {
            return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false
        }

    suspend fun <DTO : ModelDTO, DATA: DataModel> launchSequence(
        pack: SequencePack<DTO, DATA>,
    ): List<DATA> {
        val session = sessionManager.getCurrentSession().getOrDefault(sessionManager.getAnonymous())
       val result = dispatchManager.enqueue(session.principal.userId) {
             coroutineEmitter.dispatch<DTO, DATA>(pack, session.scope)
        }.await()
        return result
    }

    fun addService(service : ServiceClass<ModelDTO, DataModel, ExposifyEntityBase>){
        services.putIfAbsent(service.name, service)
    }

    fun getService(name: String): ServiceClass<ModelDTO, DataModel, ExposifyEntityBase>?{
        this.services.keys.firstOrNull{it == name}?.let { return services[it] }
        return null
    }
}