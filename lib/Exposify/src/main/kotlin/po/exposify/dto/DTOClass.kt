package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.DatabaseManager
import po.exposify.common.event.DTOClassEvent
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.bindings.BindingHub.ListData
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.configuration.setupValidation
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.throwInit
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.task.TaskHandler
import po.misc.callbacks.manager.CallbackManager
import po.misc.callbacks.manager.CallbackPayload
import po.misc.callbacks.manager.builders.callbackManager
import po.misc.callbacks.manager.models.Configuration
import po.misc.data.builders.logProxy
import po.misc.interfaces.ClassIdentity
import po.misc.interfaces.IdentifiableClass
import po.misc.interfaces.ValueBased
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import po.misc.types.TypeRecord
import po.misc.types.toSimpleNormalizedKey
import po.misc.validators.general.models.CheckStatus
import kotlin.reflect.KClass
import kotlin.reflect.KType


sealed class DTOBase<DTO, DATA, ENTITY>(
    override val identity: ClassIdentity
): ClassDTO,  InlineAction, IdentifiableClass
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    enum class Events(override val value: Int) : ValueBased{
        Initialized(1),
        StatusChanged(2),
        DelegateRegistrationComplete(11);
    }

    val notifier :  CallbackManager<Events> = callbackManager<Events>(
        config = Configuration(exceptionOnTriggerFailure = true)
    )

    val onStatusChanged: CallbackPayload<Events, DTOBase<DTO, DATA, ENTITY>>
            = CallbackManager.createPayload<Events, DTOBase<DTO, DATA, ENTITY>>(notifier, Events.StatusChanged)

    val onInitialized: CallbackPayload<Events, DTOBase<DTO, DATA, ENTITY>>
            = CallbackManager.createPayload<Events, DTOBase<DTO, DATA, ENTITY>>(notifier, Events.Initialized)

    var status: DTOClassStatus = DTOClassStatus.Uninitialized
        protected set(value) {
            if(field != value){
                info.logMessage("Changed status from ${field.name} to ${value.name}")
                field = value
                notifier.trigger(Events.StatusChanged, this)
            }
        }

    @PublishedApi
    internal var configParameter: DTOConfig<DTO, DATA, ENTITY>? = null
    val config:  DTOConfig<DTO, DATA, ENTITY>
        get() {
          return configParameter.getOrInitEx("DTOConfig uninitialized $completeName", ExceptionCode.LAZY_NOT_INITIALIZED)
        }

    protected val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    val dtoMapSize: Int get() = dtoMap.size

    internal val logger : TaskHandler<*> get() = actionHandler

    val warning = logProxy(this, DTOClassEvent.Warning){
        val event = DTOClassEvent(this, it)
        log(event, DTOClassEvent.Warning)
    }
    val success = logProxy(this, DTOClassEvent.Success){
        val event = DTOClassEvent(this, it)
        log(event, DTOClassEvent.Success)
    }
    val info = logProxy(this, DTOClassEvent.Info){
        log(DTOClassEvent(this, it), DTOClassEvent.Info)
    }

    private val registryExceptionMessage = "Can not find type record in DTOClass"

    private val registry:  TypeRegistry get()= config.registry

    abstract val dtoType : TypeRecord<DTO>

    internal val dataType : TypeRecord<DATA>
        get() = registry.getRecord<DATA, InitException>(SourceObject.Data, registryExceptionMessage)
    internal val entityType : TypeRecord<ENTITY>
        get() = registry.getRecord<ENTITY, InitException>(SourceObject.Entity, registryExceptionMessage)

    internal var delegateRegistrationForward =  CallbackManager.createPayload<Events, ListData<DTO, DATA, ENTITY>>(notifier, Events.DelegateRegistrationComplete)

    @PublishedApi
    internal abstract fun setup()

    @PublishedApi
    internal fun updateStatus(newStatus: DTOClassStatus){
        status = newStatus
    }

    @PublishedApi
    internal  fun initializationComplete(shallowDTO: CommonDTO<DTO, DATA, ENTITY>){
        status = DTOClassStatus.PreFlightCheck
        info.logMessage("Launching validation sequence")
        val validationResult = setupValidation(shallowDTO)
        if(validationResult == CheckStatus.PASSED){
            status = DTOClassStatus.Initialized
            notifier.trigger(Events.Initialized, this)
        }else{
            val root = findHierarchyRoot()
            DatabaseManager.signalCloseConnection(this, root.serviceClass.connectionClass)
            finalize()
            throwInit("DTO validation check failure", ExceptionCode.BAD_DTO_SETUP)
        }
    }

    internal fun finalize(){
        warning.logMessage("Finalizing")
        clearCachedDTOs()
        updateStatus(DTOClassStatus.Uninitialized)
        config.childClasses.forEach { it.finalize() }
    }

    internal fun registerDTO(dto: CommonDTO<DTO, DATA, ENTITY>){
        val existed = dtoMap.containsKey(dto.id)
        dtoMap[dto.id] = dto
        if (existed) {
            val handler = actionHandler
            handler.warn("Given dto with id: ${dto.id} already exist in dtoMap")
        }
    }

    internal fun clearCachedDTOs(){
        dtoMap.clear()
    }

    internal fun lookupDTO(id: Long): CommonDTO<DTO, DATA, ENTITY>?{
        return dtoMap[id]
    }
    internal fun lookupDTO(id: Long, operation: CrudOperation): CommonDTO<DTO, DATA, ENTITY>?{
        return dtoMap[id]?.addTrackerInfo(operation, this)
    }
    internal fun lookupDTO(): List<CommonDTO<DTO, DATA, ENTITY>>{
       return dtoMap.values.toList()
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(config.entityModel.table)
        config.childClasses.forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }

    fun getEntityModel(): ExposifyEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<ExposifyEntityClass<ENTITY>>()
    }

    fun findHierarchyRoot(): RootDTO<*, *, *>{
        return when(this){
            is RootDTO-> this
            is DTOClass-> parentClass.findHierarchyRoot()
        }
    }

    fun whereQuery(): WhereQuery<IdTable<Long>> {
        return  WhereQuery(config.entityModel.sourceTable)
    }

    fun serializerLookup(type: KType): SerializerInfo<*>?{
        val normalizedKey = type.toSimpleNormalizedKey()
        when(this){
            is RootDTO->{
                val serializersMap = serviceContext.serviceClass.connectionClass.serializerMap
                return serializersMap[normalizedKey]
            }
            is DTOClass ->{
                findHierarchyRoot().let {hierarchyRoot->
                    val serializersMap =  hierarchyRoot.serviceContext.serviceClass.connectionClass.serializerMap
                    return serializersMap[normalizedKey]
                }
            }
        }
    }
}

abstract class RootDTO<DTO, DATA, ENTITY>(
    private val clazz: KClass<DTO>
): DTOBase<DTO, DATA, ENTITY>(ClassIdentity("RootDTO", clazz.simpleName.toString())), TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{


    override val dtoType: TypeRecord<DTO> =  TypeRecord.createRecord(SourceObject.DTO, clazz)

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null
    @PublishedApi
    internal val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInitEx()

    internal val serviceClass: ServiceClass<DTO, DATA, ENTITY>
        get() = serviceContext.serviceClass


    internal fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if(status == DTOClassStatus.Uninitialized){
            info.logMessage("Launching initialization sequence")
            setup()
        }
    }

    fun getServiceClass(): ServiceClass<DTO, DATA, ENTITY>{
       return serviceContext.serviceClass.getOrInitEx("ServiceClass not assigned for $completeName")
    }

    fun switchQuery(id: Long): SwitchQuery<DTO, DATA, ENTITY> {
        return SwitchQuery(id, this)
    }
    companion object: ValueBased{
        override val value: Int
            get() = 1
    }
}

abstract class DTOClass<DTO, DATA, ENTITY>(
    val clazz: KClass<DTO>,
    val parentClass: DTOBase<*, *, *>,
): DTOBase<DTO, DATA, ENTITY>(ClassIdentity("DTOClass", clazz.simpleName.toString())), ClassDTO, TasksManaged
        where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity
{

    override val dtoType: TypeRecord<DTO> = TypeRecord.createRecord(SourceObject.DTO, clazz)

    @PublishedApi
    internal fun initialization() {
        if(status == DTOClassStatus.Uninitialized){
            setup()
        }
    }



    companion object: ValueBased{
        override val value: Int
            get() = 2
    }

}

