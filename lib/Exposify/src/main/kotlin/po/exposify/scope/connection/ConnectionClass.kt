package po.exposify.scope.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.controls.ConnectionInfo
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.session.CoroutineSessionHolder
import po.lognotify.eventhandler.EventHandlerBase
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty1

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

    private val dispatchManager = UserDispatchManager()
    private val coroutineEmitter = CoroutineEmitter("${connectionInfo.dbName}|CoroutineEmitter",eventHandler)

    val isConnectionOpen: Boolean
        get(){return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false  }

    suspend fun <DATA : DataModel, ENTITY: LongEntity>launchSequence(
        pack : SequencePack<DATA, ENTITY>,
        parentEventHandler: RootEventHandler //Temporary solution unless sessions will get introduced
        ): Deferred<List<DATA>> {

        val session = CoroutineSessionHolder.getCurrentContext()
        val userId = session?.userId

        return if (userId != null) {
            CoroutineScope(Dispatchers.IO).async {
                dispatchManager.queManager.enqueue(userId, this) {
                    coroutineEmitter.dispatch(pack).await()
                }
            }
        } else {
            coroutineEmitter.dispatch(pack)
        }
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