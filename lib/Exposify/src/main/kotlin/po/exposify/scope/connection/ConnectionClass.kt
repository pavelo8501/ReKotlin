package po.exposify.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.interfaces.ManagedSession
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.sequence.models.RootSequencePack
import po.exposify.scope.service.ServiceClass
import po.misc.exceptions.CoroutineInfo
import kotlin.coroutines.coroutineContext

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

    val isConnectionOpen: Boolean
        get() { return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false }

    suspend fun requestEmitter(session: AuthorizedSession): CoroutineEmitter {
        val result = dispatchManager.enqueue(session.sessionID) {
            CoroutineEmitter("CoroutineEmitter${CoroutineInfo.createInfo(coroutineContext).name}", session)
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