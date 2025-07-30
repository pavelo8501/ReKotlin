package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.DatabaseManager
import po.exposify.common.classes.exposifyDebugger
import po.exposify.common.event.DTOClassData
import po.exposify.common.events.ContextData
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.DTOConfiguration
import po.exposify.dto.components.ExecutionContext
import po.exposify.dto.components.RootExecutionContext
import po.exposify.dto.components.bindings.helpers.shallowDTO
import po.exposify.dto.configuration.setupValidation
import po.exposify.dto.enums.DTOClassStatus
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.initException
import po.exposify.extensions.getOrInit
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.tasks.TaskHandler
import po.lognotify.debug.debugProxy
import po.misc.context.CTX
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.processors.SeverityLevel
import po.misc.functions.subscribers.TaggedLambdaRegistry
import po.misc.interfaces.ValueBased
import po.misc.validators.models.CheckStatus



sealed class DTOBase<DTO, D, E>(
    internal val commonDTOType:CommonDTOType<DTO, D, E>,
    val dtoConfiguration:  DTOConfig<DTO, D, E> = DTOConfig(commonDTOType)
): DTOConfiguration<DTO, D, E> by dtoConfiguration, ClassDTO, TasksManaged where DTO: ModelDTO, D : DataModel, E : LongEntity
{
    enum class Events(override val value: Int) : ValueBased{
        Initialized(1),
        StatusChanged(2),
        NewHierarchyMember(3)
    }

    abstract override val identity: CTXIdentity<out CTX>

    private val keyType: Class<Events> = Events::class.java
    internal val onStatusChanged: TaggedLambdaRegistry<Events, DTOBase<DTO, D, E>> = TaggedLambdaRegistry(keyType)
    internal val onInitialized: TaggedLambdaRegistry<Events, DTOBase<DTO, D, E>> = TaggedLambdaRegistry(keyType)
    internal val onNewMember: TaggedLambdaRegistry<Events, DTOClass<*, *, *>> = TaggedLambdaRegistry(keyType)

    var status: DTOClassStatus = DTOClassStatus.Uninitialized
        private set

    protected val dtoMap : MutableMap<Long, CommonDTO<DTO, D, E>> = mutableMapOf()

    val dtoMapSize: Int get() = dtoMap.size

    internal val logger : TaskHandler<*> get() = taskHandler

   internal val debug = debugProxy(this, DTOClassData){
        DTOClassData(this, it.message, status.name, dtoMapSize)
    }

    val debugger = exposifyDebugger(this, ContextData){
        ContextData(it.message)
    }

    val entityClass: ExposifyEntityClass<E> get() = commonDTOType.entityType.entityClass

    abstract val serviceClass: ServiceClass<*, *, *>

    init {
        withTransactionIfNone(debugger, false){
            commonDTOType.initializeColumnMetadata()
        }
    }

    abstract fun setup()

    @PublishedApi
    internal fun updateStatus(newStatus: DTOClassStatus){
        this.notify("$this Changed status from ${status.name} to ${newStatus.name}", SeverityLevel.INFO)

        status = newStatus

        onStatusChanged.trigger(Events.StatusChanged, this)
        if(newStatus == DTOClassStatus.Initialized){
            onInitialized.trigger(Events.Initialized, this)
        }
    }

    @PublishedApi
    internal fun initializationComplete(validationResult : CheckStatus){

        if(validationResult == CheckStatus.PASSED){
            updateStatus(DTOClassStatus.Initialized)
        }else{
            val root = findHierarchyRoot()
            DatabaseManager.signalCloseConnection(this, root.serviceClass.connectionClass)
            finalize()
            throw initException("DTO validation check failure", ExceptionCode.BAD_DTO_SETUP, this)
        }
    }

    internal fun finalize(){
        notify("Finalizing", SeverityLevel.WARNING)
        clearCachedDTOs()
        updateStatus(DTOClassStatus.Uninitialized)
        dtoConfiguration.childClasses.values.forEach { it.finalize() }
    }

    internal fun registerDTO(dto: CommonDTO<DTO, D, E>){
       val usedId = if(dto.id < 1){
           dto.dtoId.id
        }else{
           dto.id
       }
       val exists = dtoMap.containsKey(usedId)
        if(exists){
            notify("Given dto with id: ${dto.id} already exist in dtoMap", SeverityLevel.WARNING)
        }
        dtoMap.putIfAbsent(usedId, dto)
    }

    internal fun clearCachedDTOs(){
        dtoMap.clear()
    }

    internal fun lookupDTO(id: Long): CommonDTO<DTO, D, E>?{
        return dtoMap[id]?:run {
            dtoMap.values.firstOrNull {it.id == id}
        }
    }
    internal fun lookupDTO(): List<CommonDTO<DTO, D, E>>{
       return dtoMap.values.toList()
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(entityClass.table)
        dtoConfiguration.childClasses.values.forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }

    fun findHierarchyRoot(): RootDTO<*, *, *>{
        return when(this){
            is RootDTO-> this
            is DTOClass-> parentClass.findHierarchyRoot()
        }
    }


    override fun toString(): String  = identity.identifiedByName

}

abstract class RootDTO<DTO, DATA, ENTITY>(
    commonType:CommonDTOType<DTO, DATA, ENTITY>
): DTOBase<DTO, DATA, ENTITY>(commonType), ClassDTO
        where DTO: ModelDTO, DTO:CTX, DATA: DataModel, ENTITY: LongEntity
{

    override val identity: CTXIdentity<RootDTO<DTO, DATA, ENTITY>> = asIdentity()

    private var serviceContextParameter: ServiceContext<DTO, DATA, ENTITY>? = null
    @PublishedApi
    internal val serviceContext: ServiceContext<DTO, DATA, ENTITY>
        get() = serviceContextParameter.getOrInit(this)

    override val serviceClass: ServiceClass<DTO, DATA, ENTITY>
        get() = serviceContext.serviceClass

    internal val executionContext: RootExecutionContext<DTO, DATA, ENTITY> = RootExecutionContext(this)

    init {
        identity.setNamePattern { "${it.className}[${commonType.dtoType.simpleName}]" }
    }

    internal fun runValidation(){
        updateStatus(DTOClassStatus.PreFlightCheck)
        setupValidation(shallowDTO())
    }

    internal fun initialization(serviceContext: ServiceContext<DTO, DATA, ENTITY>) {
        serviceContextParameter = serviceContext
        if(status == DTOClassStatus.Uninitialized){
            notify("Launching initialization sequence", SeverityLevel.INFO)
            setup()
        }
    }

    companion object: ValueBased{
        override val value: Int
            get() = 1
    }
}

abstract class DTOClass<DTO, DATA, ENTITY>(
    commonType:CommonDTOType<DTO, DATA, ENTITY>,
    val parentClass: DTOBase<*, *, *>,
): DTOBase<DTO, DATA, ENTITY>(commonType), ClassDTO, TasksManaged where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity
{
    override val identity: CTXIdentity<DTOClass<DTO, DATA, ENTITY>> = asIdentity()

    override val serviceClass: ServiceClass<*, *, *> get() =  findHierarchyRoot().serviceClass

    init {
        identity.setNamePattern { "${it.className}[${commonType.dtoType.simpleName}]" }
    }

    internal fun <F: ModelDTO, FD: DataModel, FE: LongEntity> runValidation(
        parentDTO: CommonDTO<F, FD, FE>
    ){
        updateStatus(DTOClassStatus.PreFlightCheck)
        setupValidation(shallowDTO(parentDTO))
    }

    @PublishedApi
    internal fun initialization():DTOClass<DTO, DATA, ENTITY> {
        if(status == DTOClassStatus.Uninitialized){
            setup()
        }

        return this
    }

    companion object: ValueBased{
        override val value: Int
            get() = 2
    }

}

