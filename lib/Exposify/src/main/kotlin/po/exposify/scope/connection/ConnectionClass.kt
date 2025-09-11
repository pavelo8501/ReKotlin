package po.exposify.scope.connection

import kotlinx.coroutines.currentCoroutineContext
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transactionManager
import po.auth.sessions.models.AuthorizedSession
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
    internal  var services: MutableMap<CommonDTOType<*, *, *>, ServiceClass<*, *, *>> = mutableMapOf()

    init {
        notify("CONNECTION_CLASS CREATED $completeName")
    }

    internal suspend fun requestEmitter(process: Process<AuthorizedSession>): CoroutineEmitter {
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
            services[dtoClass.commonDTOType] = serviceClass
        }

//        if(existentService == null){
//            val serviceClass = ServiceClass(dtoClass, this)
//            notify("ServiceClass ${serviceClass.contextName} created", SeverityLevel.INFO)
//            serviceClass.initService(dtoClass, createOptions, block)
//            servicesBacking[dtoClass.commonDTOType] = serviceClass
//        }else{
//            notify("Using ServiceClass ${existentService.contextName}", SeverityLevel.INFO)
//            block.invoke(existentService.serviceContext)
//        }
    }.resultOrException()

    fun clearServices(){
        services.clear()
    }
}