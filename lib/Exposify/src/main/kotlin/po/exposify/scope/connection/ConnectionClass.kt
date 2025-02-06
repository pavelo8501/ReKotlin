package po.exposify.scope.connection

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.controls.ConnectionInfo
import po.db.data_service.scope.connection.controls.CoroutineEmitter
import po.db.data_service.scope.sequence.models.SequencePack
import po.db.data_service.scope.service.ServiceClass
import kotlin.coroutines.CoroutineContext

class ConnectionClass(
    val connectionInfo: ConnectionInfo,
    val connection: Database,
) {

    var services = mutableMapOf<String,ServiceClass<*,*>>()

    init {
        connectionInfo.connection
    }

    val coroutineEmitter = CoroutineEmitter("${connectionInfo.dbName}|CoroutineEmitter")

    val isConnectionOpen: Boolean
        get(){return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false  }

    fun <DATA : DataModel, ENTITY: LongEntity>launchSequence(pack : SequencePack<DATA, ENTITY>){
        coroutineEmitter.dispatch(pack)
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