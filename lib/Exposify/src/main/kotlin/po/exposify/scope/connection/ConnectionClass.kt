package po.exposify.scope.connection

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceClass
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.coroutines.CoroutineContext

class ConnectionClass(
    val connectionInfo: ConnectionInfo,
    val connection: Database,
) : CanNotify {

    val name = "ConnectionClas|{$connection.name}"

    var services = mutableMapOf<String,ServiceClass<*,*>>()

    override var eventHandler = RootEventHandler(name)

    init {
        connectionInfo.connection
    }

    val coroutineEmitter = CoroutineEmitter("${connectionInfo.dbName}|CoroutineEmitter",eventHandler)

    val isConnectionOpen: Boolean
        get(){return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false  }

    fun <DATA : DataModel, ENTITY: LongEntity>launchSequence(
        pack : SequencePack<DATA, ENTITY>,
        data : List<DATA>? = null,
        parentEventHandler: RootEventHandler //Temporary solution unless sessions will get introduced
        ){
        eventHandler = parentEventHandler
        coroutineEmitter.dispatch(pack, data)
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