package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.SwitchQuery
import po.exposify.dto.components.WhereQuery
import po.exposify.dto.components.bindings.BindingHub.ListData
import po.exposify.dto.components.bindings.BindingHub.NotificationData
import po.exposify.dto.components.tracker.CrudOperation
import po.exposify.dto.components.tracker.extensions.addTrackerInfo
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.models.SourceObject
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.task.TaskHandler
import po.lognotify.lastTaskHandler
import po.misc.callbacks.CallbackPayload
import po.misc.callbacks.callbackManager
import po.misc.interfaces.Identifiable
import po.misc.interfaces.ValueBased
import po.misc.interfaces.asIdentifiable
import po.misc.registries.type.TypeRegistry
import po.misc.serialization.SerializerInfo
import po.misc.types.TypeRecord
import po.misc.types.toSimpleNormalizedKey
import kotlin.reflect.KType


sealed class DTOBase<DTO, DATA, ENTITY>(): ClassDTO,  TasksManaged, ValueBased, InlineAction
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    enum class Events(override val value: Int) : ValueBased{
        ON_INITIALIZED(1),
        DelegateInitialized(10),
        DelegateRegistrationComplete(11);
    }

    abstract override val value: Int

    var status : DTOClassStatus = DTOClassStatus.Undefined

    var component: Identifiable = asIdentifiable("Undefined", "DTOBase")

    @PublishedApi
    internal var configParameter: DTOConfig<DTO, DATA, ENTITY>? = null
    val config:  DTOConfig<DTO, DATA, ENTITY>
        get() {
          return  configParameter.getOrInitEx("DTOConfig uninitialized ${component.completeName}", ExceptionCode.LAZY_NOT_INITIALIZED)
        }

    override var initialized: Boolean = false
    protected val dtoMap : MutableMap<Long, CommonDTO<DTO, DATA, ENTITY>> = mutableMapOf()

    internal val notifier = callbackManager<DTOBase<DTO, DATA, ENTITY>>()
    internal val initializationNotifier = callbackManager<NotificationData<DTO, *>>()
    internal val callbackForwarder = callbackManager<ListData<DTO, DATA, ENTITY>>()

    //protected val notifier : TypedCallbackRegistry<DTOBase<DTO, DATA, ENTITY>, Unit> = TypedCallbackRegistry()

    internal val logger : TaskHandler<*> get() = lastTaskHandler()

    private val registryExceptionMessage = "Can not find type record in DTOClass"
    private val registry:  TypeRegistry get()= config.registry
    internal val dtoType : TypeRecord<DTO>
        get() = registry.getRecord<DTO, InitException>(SourceObject.DTO, registryExceptionMessage)
    internal val dataType : TypeRecord<DATA>
        get() = registry.getRecord<DATA, InitException>(SourceObject.Data, registryExceptionMessage)
    internal val entityType : TypeRecord<ENTITY>
        get() = registry.getRecord<ENTITY, InitException>(SourceObject.Entity, registryExceptionMessage)

    init {
//        notifier.onKeyOverwrite = { overwrittenKey, _->
//            logger.warn("typedCallbackRegistry notify subscription key: $overwrittenKey overwritten")
//        }
    }

    internal fun subscribe(component: Identifiable, type: ValueBased, callback: (DTOBase<DTO, DATA, ENTITY>)-> Unit) {
        notifier.subscribe(component, CallbackPayload.create(type, callback))
    }

    @PublishedApi
    internal fun subscribeNotifier(component: Identifiable, type: ValueBased, callback: (NotificationData<DTO, *>)-> Unit) {
        initializationNotifier.subscribe(component, CallbackPayload.create(type, callback))

    }

    protected abstract fun  setup()
    @PublishedApi
    internal fun initializationComplete(){
        component = asIdentifiable(dtoType.simpleName, "BaseDTO")
        initialized = true
        notifier.triggerForAll(Events.ON_INITIALIZED, this)
    }

    internal fun registerDTO(dto: CommonDTO<DTO, DATA, ENTITY>){
        val existed = dtoMap.containsKey(dto.id)
        dtoMap[dto.id] = dto
        if (existed) {
            val handler = lastTaskHandler()
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
        return dtoMap[id]?.addTrackerInfo(operation, this.component)
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
        val normalizedKey  = type.toSimpleNormalizedKey()
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

    companion object : ValueBased{
        override val value: Int = 0
    }
}

abstract class RootDTO<DTO, DATA, ENTITY>()
    : DTOBase<DTO, DATA, ENTITY>(),  TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{
//    override val componentClass: ComponentClass<DTO>
//        get()  {
//            val classs = ComponentClass<DTO>(ComponentType.RootClass)
//            return classs
//        }

    override val value: Int = 1

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null
    val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInitEx()

    fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if (!initialized) setup()
    }
    fun reinitChil(){
        config.childClasses.forEach {
            if(!it.initialized){
                it.initialization()
            }
        }
    }

    fun getServiceClass(): ServiceClass<DTO, DATA, ENTITY>{
       return serviceContext.serviceClass.getOrInitEx("ServiceClass not assigned for ${component.completeName}")
    }

    fun switchQuery(id: Long): SwitchQuery<DTO, DATA, ENTITY> {
        return SwitchQuery(id, this)
    }

    companion object : ValueBased{
        override val value: Int = 1
    }
}

abstract class DTOClass<DTO, DATA, ENTITY>(
    val parentClass: DTOBase<*, *, *>,
): DTOBase<DTO, DATA, ENTITY>(), ClassDTO, TasksManaged
        where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity {

    override val value: Int = 2

//    override val componentClass: ComponentClass<DTO>
//        get() =  ComponentClass<DTO>(ComponentType.DTOClass)

    fun initialization() {
        if (!initialized){ setup() }
    }

    companion object : ValueBased{
        override val value: Int = 2
    }
}

