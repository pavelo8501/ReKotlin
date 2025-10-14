package po.exposify.scope.connection

import kotlinx.coroutines.currentCoroutineContext
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.models.SessionBase
import po.exposify.DatabaseManager
import po.exposify.dto.RootDTO
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.connection.controls.UserDispatchManager
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.launchers.runTask
import po.lognotify.process.Process
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.coroutines.CoroutineInfo
import po.misc.serialization.SerializerInfo
import po.misc.types.type_data.TypeData
import po.misc.types.safeCast


class ConnectionClass(
    internal val databaseManager : DatabaseManager,
    val connection: Database,
): TasksManaged {

    override val identity:  CTXIdentity<ConnectionClass> = asIdentity()
    private val dispatchManager = UserDispatchManager()

    val isConnectionOpen: Boolean
        get() = connection.transactionManager.currentOrNull()?.connection?.isClosed == false

    internal val serializerMap = mutableMapOf<String, SerializerInfo<*>>()
    internal val servicesBacking: MutableMap<CommonDTOType<*, *, *>, ServiceClass<*, *, *>> = mutableMapOf()
    val services: Map<CommonDTOType<*, *, *>, ServiceClass<*, *, *>> get() = servicesBacking

    init {
        notify("CONNECTION_CLASS CREATED $completeName")
    }

    internal suspend fun requestEmitter(process: Process<SessionBase>): CoroutineEmitter {
        val result = dispatchManager.enqueue(process.receiver.sessionID) {
            CoroutineEmitter("CoroutineEmitter${CoroutineInfo.createInfo(currentCoroutineContext()).coroutineName}", process)
        }
        return result
    }

    internal fun registerSerializer(serialInfo: SerializerInfo<*>){
        notify("CONNECTION_CLASS SERIALIZER REGISTRY NEW SERIALIZER NORMALIZED_NAME: ${serialInfo.normalizedKey}")
        serializerMap[serialInfo.normalizedKey] = serialInfo
    }

    internal fun <DTO: ModelDTO, D: DataModel, E: LongEntity> getService(
        commonType: CommonDTOType<DTO, D, E>
    ): ServiceClass<DTO, D, E>?{

       return services[commonType]?.safeCast<ServiceClass<DTO, D, E>>()
    }

    fun getServiceByDataModelType(
        dataModelTypeData: TypeData<*>
    ): ServiceContext<*, *, *> ? {
       val  connection = servicesBacking.values.firstOrNull{ it.serviceContext.dataType == dataModelTypeData }
       return connection?.serviceContext
    }


    fun close(){
        notify("Closing connection: ${connection.name}")
        TransactionManager.closeAndUnregister(database = connection)
        services.values.forEach {
            it.deinitializeService()
        }
    }

    fun <DTO, D, E> service(
        dtoClass : RootDTO<DTO, D, E>,
        createOptions : TableCreateMode = TableCreateMode.Create,
        block: (ServiceContext<DTO, D, E>.()->Unit)? = null
    ): Unit where DTO : ModelDTO, D: DataModel, E: LongEntity = runTask("service"){

        val existentService = getService(dtoClass.commonDTOType)
        if(existentService != null){
            if(dtoClass.status != DTOClassStatus.Initialized){
                existentService.initService(dtoClass, createOptions, block)
            }else{
                block?.invoke(existentService.serviceContext)
            }
            notify("Using ServiceClass ${existentService.contextName}")
        }else{
            val serviceClass = ServiceClass(dtoClass, this)
            notify("ServiceClass ${serviceClass.contextName} created")
            serviceClass.initService(dtoClass, createOptions, block)
            servicesBacking[dtoClass.commonDTOType] = serviceClass
        }
    }.resultOrException()

    fun clearServices(){
        servicesBacking.clear()
    }
}