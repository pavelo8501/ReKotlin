package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.DatabaseManager
import po.exposify.common.event.DTOClassData
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.ExecutionContext
import po.exposify.dto.components.createProvider
import po.exposify.dto.configuration.setupValidation
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.throwInit
import po.exposify.extensions.getOrInit
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.action.InlineAction
import po.lognotify.tasks.TaskHandler
import po.lognotify.debug.debugProxy
import po.misc.callbacks.CallbackManager
import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.builders.callbackManager
import po.misc.callbacks.models.Configuration
import po.misc.data.printable.printableProxy
import po.misc.exceptions.ManagedCallSitePayload
import po.misc.interfaces.ClassIdentity
import po.misc.context.IdentifiableClass
import po.misc.interfaces.ValueBased
import po.misc.serialization.SerializerInfo
import po.misc.types.TypeData
import po.misc.types.containers.TypedContainer
import po.misc.types.toSimpleNormalizedKey
import po.misc.validators.general.models.CheckStatus
import kotlin.reflect.KClass
import kotlin.reflect.KType

sealed class DTOBase<DTO, DATA, ENTITY>(
    override val identity: ClassIdentity
): ClassDTO,  InlineAction, IdentifiableClass, TasksManaged
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    enum class Events(override val value: Int) : ValueBased{
        Initialized(1),
        StatusChanged(2),
        DelegateRegistrationComplete(11);
    }

    val exPayload: ManagedCallSitePayload = ManagedCallSitePayload(this)

    val config:  DTOConfig<DTO, DATA, ENTITY> by lazy {
        DTOConfig(this)
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

    protected val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    val dtoMapSize: Int get() = dtoMap.size

    internal val logger : TaskHandler<*> get() = actionHandler

    val warning = printableProxy(this, DTOClassData.Warning){ params->
        log(DTOClassData(this, params.message), params.template)
    }
    val success = printableProxy(this, DTOClassData.Success){ params->
        log(DTOClassData(this, params.message), params.template)
    }
    val info = printableProxy(this, DTOClassData.Info){ params->
        log(DTOClassData(this, params.message), params.template)
    }

    val debug = debugProxy(this, DTOClassData){
        DTOClassData(this, it.message, status.name, dtoMapSize)
    }

    private val registryExceptionMessage = "Can not find type record in DTOClass"

    abstract val dtoType : TypeData<DTO>

    private var dataTypeBacking: TypeData<DATA>? = null
    val dataType: TypeData<DATA> get() = dataTypeBacking.getOrInit(exPayload.valueFailure("dataTypeBacking", "TypeRecord<DATA>"))
    val isDataTypeAvailable: Boolean get() = dataTypeBacking != null
    fun provideDataType(dataType: TypeData<DATA>){
        dataTypeBacking = dataType
    }

    private var entityTypeBacking: TypeData<ENTITY>? = null
    val entityType: TypeData<ENTITY> get() = entityTypeBacking.getOrInit(exPayload.valueFailure("entityTypeBacking", "TypeRecord<ENTITY>"))
    val isEntityTypeAvailable: Boolean get() = entityTypeBacking != null
    fun provideEntityType(entityType: TypeData<ENTITY>){
        entityTypeBacking = entityType
    }

    private var commonTypeBacking: CommonDTOType<DTO, DATA, ENTITY>? = null
    val commonType: CommonDTOType<DTO, DATA, ENTITY> get() {
      return commonTypeBacking.getOrInit(exPayload.valueFailure("commonTypeBacking", "CommonDTOType<DTO, DATA, ENTITY>"))
    }
    val isCommonDTOTypeAvailable: Boolean get() = commonTypeBacking != null
    fun provideCommonDTOType(commonType: CommonDTOType<DTO, DATA, ENTITY>){
        commonTypeBacking = commonType
    }

    abstract val serviceClass: ServiceClass<*,*,*>

    //internal var delegateRegistrationForward =  CallbackManager.createPayload<Events, ListData<DTO, DATA, ENTITY>>(notifier, Events.DelegateRegistrationComplete)

    abstract fun setup()

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
       val usedId = if(dto.id < 1){
           dto.dtoId.id
        }else{
           dto.id
       }
       val exists = dtoMap.containsKey(usedId)
        if(exists){
            warning.logMessage("Given dto with id: ${dto.id} already exist in dtoMap")
        }
        dtoMap.putIfAbsent(usedId, dto)
    }

    internal fun clearCachedDTOs(){
        dtoMap.clear()
    }

    internal fun lookupDTO(id: Long): CommonDTO<DTO, DATA, ENTITY>?{
        return dtoMap[id]?:run {
            dtoMap.values.firstOrNull {it.id == id}
        }
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
        return config.entityModel
    }

    fun findHierarchyRoot(): RootDTO<*, *, *>{
        return when(this){
            is RootDTO-> this
            is DTOClass-> parentClass.findHierarchyRoot()
        }
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

    override val dtoType: TypeData<DTO> by lazy { TypeData.createByKClass(clazz) }

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null
    @PublishedApi
    internal val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInit(this)

    override val serviceClass: ServiceClass<DTO, DATA, ENTITY>
        get() = serviceContext.serviceClass

    internal val executionContext: ExecutionContext<DTO, DATA, ENTITY> by lazy { createProvider() }

    internal fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if(status == DTOClassStatus.Uninitialized){
            info.logMessage("Launching initialization sequence")
            setup()
        }
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
    override val dtoType: TypeData<DTO> by lazy {  TypeData.createByKClass(clazz) }
    internal val parentTypedMap: MutableMap<TypeData<*>, TypedContainer<*>> = mutableMapOf()

    override val serviceClass: ServiceClass<*, *, *>
        get() =  findHierarchyRoot().serviceClass


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

