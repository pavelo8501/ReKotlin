package po.exposify.scope.connection

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.models.AuthorizedSession
import po.exposify.DatabaseManager
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.scope.connection.models.ConnectionInfo
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.classes.task.TaskHandler
import po.lognotify.extensions.runTask
import po.misc.coroutines.CoroutineInfo
import po.misc.serialization.SerializerInfo
import po.misc.types.safeCast
import kotlin.coroutines.coroutineContext

class ConnectionClass(
    internal val databaseManager : DatabaseManager,
    val connectionInfo: ConnectionInfo,
    val connection: Database,
): TasksManaged {

    override val contextName: String = "ConnectionClass"

    val sourceName: String = connection.name
    val name: String = "ConnectionClass[${sourceName}]"
    private val dispatchManager = UserDispatchManager()

    val isConnectionOpen: Boolean
        get() { return connectionInfo.connection.transactionManager.currentOrNull()?.connection?.isClosed == false }

    internal val serializerMap = mutableMapOf<String, SerializerInfo<*>>()
    private  var services: MutableMap<String, ServiceClass<*, *, *>> = mutableMapOf()
    private val taskHandler: TaskHandler<*> = taskHandler()

    init {
        taskHandler.warn("CONNECTION_CLASS CREATED $name")
    }

    internal suspend fun requestEmitter(session: AuthorizedSession): CoroutineEmitter {
        val result = dispatchManager.enqueue(session.sessionID) {
            CoroutineEmitter("CoroutineEmitter${CoroutineInfo.createInfo(coroutineContext).name}", session)
        }
        return result
    }

    internal fun registerSerializer(serialInfo: SerializerInfo<*>){
        taskHandler.info("CONNECTION_CLASS SERIALIZER REGISTRY NEW SERIALIZER NORMALIZED_NAME: ${serialInfo.normalizedKey}")
        serializerMap[serialInfo.normalizedKey] = serialInfo
    }

    internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> getService(name: String): ServiceClass<DTO, D, E>?{
       return services[name]?.safeCast<ServiceClass<DTO, D, E>>()
    }

    fun close(){
        taskHandler.info("Closing connection: ${connection.name}")
        TransactionManager.closeAndUnregister(database = connection)
        services.values.forEach {
            it.deinitializeService()
        }
    }

    fun <DTO, D, E> service(
        dtoClass : RootDTO<DTO, D, E>,
        createOptions : TableCreateMode = TableCreateMode.CREATE,
        block: ServiceContext<DTO, D, E>.()->Unit,
    ) where DTO : ModelDTO, D: DataModel, E: LongEntity = runTask("Create Service") { handler ->

        handler.info("Creating ServiceClass")
        val serviceClass = ServiceClass(dtoClass, this, createOptions)
        services[serviceClass.completeName] = serviceClass
        serviceClass.initService(dtoClass)
        getService<DTO, D, E>(serviceClass.completeName)?.runServiceContext(block)
    }.onFail{
        throw it
    }

    fun clearServices(){
        services.clear()
    }
}